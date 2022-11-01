using Newtonsoft.Json;
using System.Collections.Generic;

namespace DigitalIdentity.SOFD.Model
{
    public class OrgUnit
    {
        [JsonProperty("uuid")]
        public string Uuid { get; set; }

        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("masterId")]
        public string MasterId { get; set; }

        [JsonProperty("shortname")]
        public string Shortname { get; set; }

        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("cvr")]
        public long? Cvr { get; set; }

        [JsonProperty("ean")]
        public long? Ean { get; set; }

        [JsonProperty("senr")]
        public long? Senr { get; set; }

        [JsonProperty("pnr")]
        public long? Pnr { get; set; }

        [JsonProperty("costBearer")]
        public string CostBearer { get; set; }

        [JsonProperty("orgType")]
        public string OrgType { get; set; }

        [JsonProperty("orgTypeId")]
        public int? OrgTypeId { get; set; }

        [JsonProperty("parentUuid")]
        public string ParentUuid { get; set; }

        [JsonProperty("localExtensions")]
        public object LocalExtensions { get; set; }

        [JsonProperty("deleted")]
        public bool Deleted { get; set; }

        [JsonProperty("postAddresses")]
        public List<PostAddress> PostAddresses { get; set; }

        [JsonProperty("phones")]
        public List<Phone> Phones { get; set; }

        [JsonProperty("tags")]
        public List<OrgUnitTag> Tags { get; set; }

        public OrgUnit SOFDParent { get; set; }
        public OrgUnit TaggedParent { get; set; }
        public List<OrgUnit> SOFDChildren { get; set; } = new List<OrgUnit>();
        public List<OrgUnit> TaggedChildren { get; set; } = new List<OrgUnit>();
        public string OESTagValue { get; set; }
        public bool IsRoot { get; set; } = false;
        public bool IsValid { get; set; } = true;
        public List<string> ValidationErrors { get; set; } = new List<string>();

    }
}