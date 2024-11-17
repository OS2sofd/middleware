using System;
using System.Collections.Generic;
using System.Text;

namespace DigitalIdentity.SDMOX
{
    public class SDMOXSettings
    {
        public string HostName { get; set; }
        public int Port { get; set; } = 5672;
        public string VirtualHost { get; set; }
        public string UserName { get; set; }
        public string Password { get; set; }
        public string InstitutionUUID { get; set; }
        public string DeletedOrgsUuid { get; set; }
        public bool SslEnabled { get; set; } = false;
        public string CertPath { get; set; } = "";
        public string CertPassphrase { get; set; } = "";
        public string ServerName { get; set; } = "";
    }
}
