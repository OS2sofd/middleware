using DigitalIdentity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using sofd_core_sd_integration.Database;
using System;

namespace sofd_core_sd_integration
{
    public class SyncService : BaseClass<SyncService>
    {
        private readonly DatabaseContext databaseContext;
        private readonly OrgUnitSyncService orgUnitSyncService;
        private readonly PersonSyncService personSyncService;

        public SyncService(IServiceProvider sp) : base(sp)
        {
            databaseContext = sp.GetService<DatabaseContext>();
            orgUnitSyncService = sp.GetService<OrgUnitSyncService>();
            personSyncService = sp.GetService<PersonSyncService>();
        }

        public void Synchronize()
        {
            try
            {
                logger.LogInformation("SyncService executing");
                InitializeDatabase();
                orgUnitSyncService.Synchronize();
                personSyncService.Synchronize();
                logger.LogInformation("SyncService finsihed");
            }
            catch (Exception e)
            {
                logger.LogError(e, "Unhandled exception");
            }
        }

        private void InitializeDatabase()
        {
            databaseContext.Database.Migrate();
        }
    }
}