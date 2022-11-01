using Newtonsoft.Json;

namespace DigitalIdentity.SOFD.Model
{
    public class User
    {
        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("masterId")]
        public string MasterId { get; set; }

        [JsonProperty("uuid")]
        public string Uuid { get; set; }

        [JsonProperty("userId")]
        public string UserId { get; set; }

        [JsonProperty("userType")]
        public string UserType { get; set; }

        [JsonProperty("employeeId")]
        public string EmployeeId { get; set; }

        [JsonProperty("passwordExpireDate")]
        public string PasswordExpireDate { get; set; }

        [JsonProperty("localExtensions")]
        public object LocalExtensions { get; set; }

        [JsonProperty("prime")]
        public bool Prime { get; set; }
    }
}
