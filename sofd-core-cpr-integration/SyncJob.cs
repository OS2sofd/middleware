using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Quartz;
using SofdCprIntegration;
using SofdCprIntegration.Controllers;
using SofdCprIntegration.Model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using static System.Net.WebRequestMethods;

[DisallowConcurrentExecution]
public class SyncJob : IJob
{
    private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

    // Inject the DI provider
    private readonly IServiceProvider _provider;
    private readonly IConfiguration _configuration;
    private PersonContext personContext;

    public SyncJob(IConfiguration configuration, IServiceProvider provider)
    {
        _configuration = configuration;
        _provider = provider;
    }

    public Task Execute(IJobExecutionContext context)
    {
        CleanupUnusedEntries();

        FetchFromSFTP();
        
        return Task.CompletedTask;
    }

    private void CleanupUnusedEntries()
    {
        log.Info("Running SQL Cleanup job");

        // Create a new scope
        using (var scope = _provider.CreateScope())
        {
            personContext = scope.ServiceProvider.GetService<PersonContext>();

            int day = DateTime.Now.Day;            
            if (day == 28) {
                var forceReloadPersons = personContext.Person.Where(p => p.Cpr.StartsWith("28") ||
                                                                         p.Cpr.StartsWith("29") ||
                                                                         p.Cpr.StartsWith("30") ||
                                                                         p.Cpr.StartsWith("31"));
                if (forceReloadPersons.Count() > 0)
                {
                    log.Info("Removing " + forceReloadPersons.Count() + " cached persons from DB because their birthday matches today'ish");

                    personContext.Person.RemoveRange(forceReloadPersons);
                }
            }
            else if (day < 28) {
                string value = "01";

                if (day < 10) {
                    value = "0" + day;
                }
                else {
                    value = "" + day;
                }

                var forceReloadPersons = personContext.Person.Where(p => p.Cpr.StartsWith(value));
                if (forceReloadPersons.Count() > 0)
                {
                    log.Info("Removing " + forceReloadPersons.Count() + " cached persons from DB because their birthday matches today'ish");

                    personContext.Person.RemoveRange(forceReloadPersons);
                }
            }

            personContext.SaveChanges();
        }
    }

    private void FetchFromSFTP()
    {
        log.Info("Running FTP Sync Job");

        // Create a new scope
        using (var scope = _provider.CreateScope())
        {
            personContext = scope.ServiceProvider.GetService<PersonContext>();

            SFTPService sftp = new SFTPService(_configuration);
            DateTime previousDate = DateTime.Now; // overwritten below, so the value does not matter

            LastSync lastSync = personContext.LastSync.OrderBy(x => x.Id).FirstOrDefaultAsync().Result;
            if (lastSync != null)
            {
                previousDate = lastSync.LastSyncDate;
            }
            else
            {
                lastSync = new LastSync();
                previousDate = DateTime.MinValue;
            }

            List<FileDTO> files = sftp.GetNewestFiles(previousDate);
            if (files.Count > 0)
            {
                List<string> updatedPersons = new List<string>();
                List<string> badStatesAndCpr = new List<string>();
                DateTime lastWriteTime = DateTime.Now; // overwritten below, so the value does not matter
                foreach (var file in files)
                {
                    // parse the file
                    updatedPersons.AddRange(ExtractCPR(file.Content));
                    badStatesAndCpr.AddRange(ExtractBadStates(file.Content));

                    // the files are sorted in ascending order which means that the last file in the loop is the newest, so this will end up as the lastWriteTime
                    lastWriteTime = file.LastWriteTime;
                }

                log.Info("Files from CPR office contained " + updatedPersons.Count + " rows with changes");

                // update timestamp after reading from sftp
                lastSync.LastSyncDate = lastWriteTime;
                personContext.Update(lastSync);

                List<Person> persons = personContext.Person.ToList();
                List<BadState> previousBadStates = personContext.BadState.ToList();

                log.Info("Files from CPR office contained " + badStatesAndCpr.Count + " rows with bad state changes");

                // check if bad state is dead, gone or disenfranchised
                foreach (var badState in badStatesAndCpr)
                {
                    // 012 død, 017 umyndiggjort, 001 status, hvor værdi 70 = forsvundet
                    string stateCode = badState.Substring(0, 3);
                    string cpr = badState.Substring(3, 10);

                    BadState bs;

                    if (persons.Any(p => string.Equals(p.Cpr, cpr)))
                    {
                        log.Info("Found bad state on " + cpr);

                        if (previousBadStates.Any(s => s.Cpr == cpr))
                        {
                            bs = (BadState)previousBadStates.Select(s => s.Cpr == cpr);
                            if (int.Parse(stateCode) == 017) { bs.Disenfranchised = true; }
                            if (int.Parse(stateCode) == 001) { bs.Gone = true; }
                            if (int.Parse(stateCode) == 012) { bs.IsDead = true; }
                            personContext.BadState.Update(bs);
                        }
                        else
                        {
                            bs = new BadState()
                            {
                                Cpr = cpr,
                                Tts = DateTime.Now,
                                Disenfranchised = false,
                                Gone = false,
                                IsDead = false
                            };
                            if (int.Parse(stateCode) == 017) { bs.Disenfranchised = true; }
                            if (int.Parse(stateCode) == 001) { bs.Gone = true; }
                            if (int.Parse(stateCode) == 012) { bs.IsDead = true; }
                            personContext.BadState.Add(bs);
                        }
                    }
                }

                // remove updated persons from cache-db
                personContext.Person.RemoveRange(personContext.Person.Where(p => updatedPersons.Contains(p.Cpr)));
                personContext.SaveChanges();
            }
        }

        log.Info("Completed FTP Sync Job");
    }

    private List<string> ExtractCPR(string content)
    {
        return content.Split(new string[] { System.Environment.NewLine }, StringSplitOptions.None) //split string into lines
                        .Where(l => !l.StartsWith("0000") && !l.StartsWith("9999") && !string.IsNullOrWhiteSpace(l)) //skip empty lines and start & end line
                        .Select(l => l.Substring(3, 10)) // cut only the cpr
                        .Distinct() // remove duplicates
                        .ToList(); // return as list
    }

    private List<string> ExtractBadStates(string content)
    {
        List<string> strings = new List<string>();

        using (StringReader sr = new StringReader(content))
        {
            while (sr.Peek() > -1)
            {
                var l = sr.ReadLine();
                // 012 død, hvor plads 13 = D, 017 umyndiggjort, 001 status, hvor værdi 70 = forsvundet                 
                var dog = l.Split(null);
                if ((l.StartsWith("012") && l.Substring(13, 1) == "D") ||
                     l.StartsWith("017") ||
                    (l.StartsWith("001") && l.Split(null)[10].StartsWith("70")))
                {
                    strings.Add(l.Substring(0, 13));
                }
            }
        }

        return strings;
    }
}
