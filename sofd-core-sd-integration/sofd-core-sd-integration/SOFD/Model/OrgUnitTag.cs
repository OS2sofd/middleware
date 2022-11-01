using Newtonsoft.Json;

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
