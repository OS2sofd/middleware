using System.Collections.Generic;

namespace DigitalIdentity.SD.Model
{
    public class FunkOrgEnhed
    {
        public string uuid { get; set; }
        public string navn { get; set; }
        public List<FunkOrgFunktion> orgFunktioner { get; set; }
    }
}