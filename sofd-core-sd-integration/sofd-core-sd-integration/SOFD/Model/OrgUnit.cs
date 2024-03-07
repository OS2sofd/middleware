using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
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

        [JsonProperty("manager")]
        public Manager Manager { get; set; }
        public OrgUnit Parent { get; set; }        
        public OrgUnit ParentSDUnit { get; set; }
        public int Level { get; set; }
        public bool IsSDTagged { get; set; }
        public bool IsNYUnit { get; set; }                
        public string NYCode { get; set; }
        public string NUVCode { get; set; }
    }
}
