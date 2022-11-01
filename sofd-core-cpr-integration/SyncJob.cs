using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Quartz;
using SofdCprIntegration;
using SofdCprIntegration.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

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
                DateTime lastWriteTime = DateTime.Now; // overwritten below, so the value does not matter
                foreach (var file in files)
                {
                    // parse the file
                    updatedPersons.AddRange(ExtractCPR(file.Content));

                    // the files are sorted in ascending order which means that the last file in the loop is the newest, so this will end up as the lastWriteTime
                    lastWriteTime = file.LastWriteTime;
                }

                log.Info("Files from CPR office contained " + updatedPersons.Count + " numbers with changes");

                // update timestamp after reading from sftp
                lastSync.LastSyncDate = lastWriteTime;
                personContext.Update(lastSync);

                // remove updated persons from cache-db
                personContext.Person.RemoveRange(personContext.Person.Where(p => updatedPersons.Contains(p.Cpr)));

                personContext.SaveChanges();
            }
        }
    }

    private List<string> ExtractCPR(string content)
    {
        return content.Split(new string[] { System.Environment.NewLine }, StringSplitOptions.None) //split string into lines
                        .Where(l => !l.StartsWith("0000") && !l.StartsWith("9999") && !string.IsNullOrWhiteSpace(l)) //skip empty lines and start & end line
                        .Select(l => l.Substring(3, 10)) // cut only the cpr
                        .Distinct() // remove duplicates
                        .ToList(); // return as list
    }
}
