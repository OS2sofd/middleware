using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace DigitalIdentity.SOFD.Model
{
    public class Affiliation
    {
        [JsonProperty("master")]
        public string Master { get; set; }

        [JsonProperty("masterId")]
        public string MasterId { get; set; }

        [JsonProperty("uuid")]
        public string Uuid { get; set; }

        [JsonProperty("startDate")]
        [JsonConverter(typeof(JsonDateConverter), "yyyy-MM-dd")]
        public DateTime? StartDate { get; set; }

        [JsonProperty("stopDate")]
        [JsonConverter(typeof(JsonDateConverter), "yyyy-MM-dd")]
        public DateTime? StopDate { get; set; }

        [JsonProperty("employeeId")]
        public string EmployeeId { get; set; }

        [JsonProperty("employmentTerms")]
        public string EmploymentTerms { get; set; }

        [JsonProperty("employmentTermsText")]
        public string EmploymentTermsText { get; set; }

        [JsonProperty("localExtensions")]
        public object LocalExtensions { get; set; }

        [JsonProperty("payGrade")]
        public string PayGrade { get; set; }

        [JsonProperty("workingHoursDenominator")]
        public decimal? WorkingHoursDenominator { get; set; }

        [JsonProperty("workingHoursNumerator")]
        public decimal? WorkingHoursNumerator { get; set; }

        [JsonProperty("affiliationType")]
        public string AffiliationType { get; set; }

        [JsonProperty("positionId")]
        public string PositionId { get; set; }

        [JsonProperty("positionName")]
        public string PositionName { get; set; }

        [JsonProperty("positionTypeId")]
        public string PositionTypeId { get; set; }

        [JsonProperty("positionTypeName")]
        public string PositionTypeName { get; set; }

        [JsonProperty("functions")]
        public List<string> Functions { get; set; }

        [JsonProperty("managerForUuids")]
        public List<string> ManagerForUuids { get; set; }

        [JsonProperty("deleted")]
        public bool Deleted { get; set; }

        [JsonProperty("orgUnitUuid")]
        public string OrgUnitUuid { get; set; }

        [JsonProperty("prime")]
        public bool Prime { get; set; }

        [JsonProperty("inheritPrivileges")]
        public bool InheritPrivileges { get; set; }

        public bool isActive()
        {
            return (StartDate is null || StartDate.Value.Date <= DateTime.Now.Date) && (StopDate is null || StopDate.Value.Date >= DateTime.Now.Date);
        }
        public bool isActiveOrFutureActive()
        {
            return isActive() || (StartDate != null && StartDate.Value.Date > DateTime.Now.Date);
        }

    }
}
