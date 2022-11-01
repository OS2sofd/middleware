using DigitalIdentity.CloudWatch;
using DigitalIdentity.SD;
using DigitalIdentity.SDMOX;
using DigitalIdentity.SOFD;
using sofd_core_sd_integration.Database;
using System.Collections.Generic;

namespace sofd_core_sd_integration
{
    public class AppSettings
    {
        public CloudWatchSettings CloudWatchSettings { get; set; }
        public SOFDSettings SOFDSettings { get; set; }
        public SDSettings SDSettings { get; set; }
        public DatabaseSettings DatabaseSettings { get; set; }
        public SDMOXSettings SDMOXSettings { get; set; }
        public decimal DefaultOccupationHours { get; set; }
        public Dictionary<string, decimal> PositionOccupationHoursMap { get; set; }
        public bool DryRun { get; set; }
        public bool OrgSyncEnabled { get; set; }
        public bool EmployeeSyncEnabled { get; set; }
        public bool FunctionSyncEnabled { get; set; }
    }
}
