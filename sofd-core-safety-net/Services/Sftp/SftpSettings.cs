namespace sofd_core_safety_net.Services.Sftp
{
    public class SftpSettings
    {
        public bool DryRun { get; set; }
        public string DryRunFilePath { get; set; }
        public string Username { get; set; }
        public string Password { get; set; }
        public string Host { get; set; }
        public int Port { get; set; }
    }
}
