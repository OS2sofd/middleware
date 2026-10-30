using sofd_core_safety_net.Amazon;
using sofd_core_safety_net.Jobs;
using sofd_core_safety_net.Services.Sftp;
using sofd_core_safety_net.Services.SafetyNet;
using sofd_core_safety_net.Sofd;

namespace sofd_core_safety_net.Config
{
    public class Settings
    {
        public SafetyNetSettings SafetyNetSettings { get; set; }
        public SofdSettings SofdSettings { get; set; }
        public SftpSettings SftpSettings { get; set; }
        public JobSettings JobSettings { get; set; }
        public AmazonSettings AmazonCloudWatch { get; set; }
    }
}
