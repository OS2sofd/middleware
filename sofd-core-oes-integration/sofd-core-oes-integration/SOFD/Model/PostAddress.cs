using Newtonsoft.Json;

namespace DigitalIdentity.SOFD.Model
{
    public class PostAddress
    {
        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("masterId")]
        public string MasterId { get; set; }

        [JsonProperty("street")]
        public string Street { get; set; }

        [JsonProperty("postalCode")]
        public string PostalCode { get; set; }

        [JsonProperty("city")]
        public string City { get; set; }

        [JsonProperty("localname")]
        public string Localname { get; set; }

        [JsonProperty("country")]
        public string Country { get; set; }

        [JsonProperty("addressProtected")]
        public bool AddressProtected { get; set; }

        [JsonProperty("prime")]
        public bool Prime { get; set; }
    }
}
