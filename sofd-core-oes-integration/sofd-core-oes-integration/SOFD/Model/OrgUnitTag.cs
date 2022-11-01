using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;

namespace DigitalIdentity.SOFD.Model
{
    public class OrgUnitTag
    {
        [JsonProperty("tag")]
        public string Tag { get; set; }

        [JsonProperty("customValue")]
        public string CustomValue { get; set; }

    }
}
