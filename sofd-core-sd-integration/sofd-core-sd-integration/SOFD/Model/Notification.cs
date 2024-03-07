using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;

namespace sofd_core_sd_integration.SOFD.Model
{
    public class Notification
    {
        [JsonProperty("affectedEntityUuid")]
        public string AffectedEntityUuid { get; set; }

        [JsonProperty("affectedEntityName")]
        public string AffectedEntityName { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }

        [JsonProperty("eventDate")]
        [JsonConverter(typeof(JsonDateConverter), "yyyy-MM-dd")]
        public DateTime EventDate { get; set; }

        [JsonProperty("affectedEntityType")]
        public string AffectedEntityType { get; } = "ORGUNIT";

        [JsonProperty("notificationType")]
        public string NotificationType { get; } = "UNMATCHED_WAGES_ORGUNIT";
    }
}