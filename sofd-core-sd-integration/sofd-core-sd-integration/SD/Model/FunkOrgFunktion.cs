using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace DigitalIdentity.SD.Model
{
    public class FunkOrgFunktion
    {
        public string uuid { get; set; }
        public string navn { get; set; }
        public string klasseUuid { get; set; }
        public string klasseNavn { get; set; }
        
        [JsonConverter(typeof(JsonDateConverter), "dd-MM-yyyy")]
        public DateTime start { get; set; }
        [JsonConverter(typeof(JsonDateConverter), "dd-MM-yyyy")]
        public DateTime slut { get; set; }
        public List<FunkPerson> personer { get; set; }
    }
}