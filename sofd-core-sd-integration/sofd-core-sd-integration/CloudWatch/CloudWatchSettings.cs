namespace DigitalIdentity.CloudWatch
{
    public class CloudWatchSettings
    {
        public bool Enabled { get; set; }
        public string Region { get; set; }        
        public string LogGroup { get; set; }
        public string LogStream { get; set; }
    }
}
