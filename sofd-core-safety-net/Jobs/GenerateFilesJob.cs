using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Quartz;
using sofd_core_safety_net.Services.Sftp;
using sofd_core_safety_net.Services.SafetyNet;
using sofd_core_safety_net.Services.Sofd;
using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading.Tasks;

namespace sofd_core_safety_net.Jobs
{
    [DisallowConcurrentExecution]
    internal class GenerateFilesJob : JobBase<GenerateFilesJob>
    {
        private readonly SofdService sofdService;
        private readonly SafetyNetService safetyNetService;
        private readonly SftpService ftpService;

        public GenerateFilesJob(IServiceProvider sp) : base(sp)
        {
            sofdService = sp.GetService<SofdService>();
            safetyNetService = sp.GetService<SafetyNetService>();
            ftpService = sp.GetService<SftpService>();
        }

        public override Task Execute(IJobExecutionContext context)
        {
            try
            {
                logger.LogDebug("Executing job");
                Stopwatch stopWatch = new Stopwatch();
                stopWatch.Start();

                var orgUnits = sofdService.GetOrgUnits();
                var persons = sofdService.GetPersons();
                var enc1252 = CodePagesEncodingProvider.Instance.GetEncoding(1252);

                var csvOrgUnits = safetyNetService.GenerateCSVOrgUnits(orgUnits);
                var csvOrgUnitsBytes = enc1252.GetBytes(csvOrgUnits);
                ftpService.UploadFile(new MemoryStream(csvOrgUnitsBytes), settings.SafetyNetSettings.OrgUnitFileName);

                var csvPerson = safetyNetService.GenerateCSVPersons(persons, orgUnits);
                var csvPersonsBytes = enc1252.GetBytes(csvPerson);
                ftpService.UploadFile(new MemoryStream(csvPersonsBytes), settings.SafetyNetSettings.PersonFileName);

                stopWatch.Stop();
                logger.LogDebug($"Finsihed executing job in {stopWatch.ElapsedMilliseconds / 1000} seconds");
                return Task.CompletedTask;
            }
            catch (Exception e)
            {
                logger.LogError(e, "Failed to execute job");
                return Task.FromException(e);
            }
        }
    }
}