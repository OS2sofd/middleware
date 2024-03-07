using System.Collections.Generic;

namespace DigitalIdentity.SD
{
    public class SDSettings
    {
        public string Username { get; set; }
        public string Password { get; set; }
        public string BaseUrl { get; set; }
        public List<Institution> Institutions { get; set; } = new List<Institution>();
        public string FunkBaseUrl { get; set; }
        public string FunkTopInstUuid { get; set; }
        public bool UseCodes { get; set; } = false;

        public class Institution
        {
            public string Identifier { get; set; }
            public bool Prime { get; set; } = false;
            public List<Mapping> Mappings { get; set; } = new List<Mapping>();
        }

        public class Mapping
        {
            public string SDUuid { get; set; }
            public string SOFDUuid { get; set; }
        }
    }

}
