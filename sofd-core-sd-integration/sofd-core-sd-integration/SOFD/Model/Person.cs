using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace DigitalIdentity.SOFD.Model
{
    public class Person : ChangeTrackable
    {
        [JsonProperty("cpr")]
        public string Cpr { get; set; }

        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("firstname")]
        public string Firstname { get; set; }

        [JsonProperty("surname")]
        public string Surname { get; set; }

        [JsonProperty("registeredPostAddress")]
        public PostAddress RegisteredPostAddress { get; set; }

        [JsonProperty("affiliations")]
        public List<Affiliation> Affiliations { get; set; } = new List<Affiliation>();

        [JsonProperty("firstEmploymentDate")]
        [JsonConverter(typeof(JsonDateConverter), "yyyy-MM-dd")]
        public DateTime? FirstEmploymentDate { get; set; }

        [JsonProperty("anniversaryDate")]
        [JsonConverter(typeof(JsonDateConverter), "yyyy-MM-dd")]
        public DateTime? AnniversaryDate { get; set; }

        [JsonProperty("uuid")]
        public string Uuid { get; set; }

        [JsonProperty("chosenName")]
        public string ChosenName { get; set; }

        public string GetName()
        {
            return ChosenName ?? $"{Firstname} {Surname}";
        }
        
    }
}
