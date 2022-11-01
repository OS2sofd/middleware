using Newtonsoft.Json;

namespace DigitalIdentity.SOFD.Model
{
    public class Phone
    {
        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("masterId")]
        public string MasterId { get; set; }

        [JsonProperty("phoneNumber")]
        public string PhoneNumber { get; set; }

        [JsonProperty("phoneType")]
        public string PhoneType { get; set; }

        [JsonProperty("visibility")]
        public string Visibility { get; set; }

        [JsonProperty("functionTypeId")]
        public int FunctionTypeId { get; set; }

        [JsonProperty("functionTypeName")]
        public string FunctionTypeName { get; set; }

        [JsonProperty("prime")]
        public bool Prime { get; set; }

        [JsonProperty("typePrime")]
        public bool TypePrime { get; set; }
    }
}
