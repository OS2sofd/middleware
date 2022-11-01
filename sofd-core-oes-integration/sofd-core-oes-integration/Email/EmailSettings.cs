namespace DigitalIdentity.Email
{
    public class EmailSettings
    {
        public string Host { get; set; }
        public int Port { get; set; }
        public string UserName { get; set; }
        public string Password { get; set; }        
        public string FromAddress { get; set; }
        public string ToAddress { get; set; }
        public bool Enabled { get; set; }
    }
}
