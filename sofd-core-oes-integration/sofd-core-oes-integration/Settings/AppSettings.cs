using DigitalIdentity.CloudWatch;
using DigitalIdentity.Email;
using DigitalIdentity.S3;
using DigitalIdentity.SOFD;
using sofd_core_oes_integration.Database;
using System.Collections.Generic;

namespace sofd_core_oes_integration.Settings
{
    public class AppSettings
    {
        public CloudWatchSettings CloudWatchSettings { get; set; }
        public SOFDSettings SOFDSettings { get; set; }
        public DatabaseSettings DatabaseSettings { get; set; }
        public EmailSettings EmailSettings { get; set; }
        public S3Settings S3Settings { get; set; }
        public int ActiveOrgUnitFailSafeCount { get; set; }
        public List<string> AllowedHierarchyMismatchUuids { get; set; }
    }
}
