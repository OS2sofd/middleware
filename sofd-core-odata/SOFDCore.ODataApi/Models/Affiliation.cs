using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_affiliation")]
    public class Affiliation
    {
        [Key]
        public string Uuid { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? StopDate { get; set; }
        public bool? Deleted { get; set; }
        public string EmployeeId { get; set; }
        public string EmploymentTerms { get; set; }
        public string EmploymentTermsText { get; set; }
        public string PayGrade { get; set; }
        public string WageStep { get; set; }
        public double? WorkingHoursDenominator { get; set; }
        public double? WorkingHoursNumerator { get; set; }
        public string AffiliationType { get; set; }
        public string LocalExtensions { get; set; }
        public string PositionId { get; set; }
        public string PositionName { get; set; }
        public bool? Prime { get; set; }
        public string PositionTypeId { get; set; }
        public string PositionTypeName { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }

        [ForeignKey("OrgUnit")]
        public string OrgunitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }

        public string SourceOrgunitUuid { get; set; }
        public string AlternativeOrgunitUuid { get; set; }

        public ICollection<AffiliationKLEPrimary> KLEPrimary { get; set; }
        public ICollection<AffiliationKLESecondary> KLESecondary { get; set; }
        public ICollection<AffiliationFunction> Functions { get; set; }
    }
}
