using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit")]
    public class OrgUnit
    {
        [Key]
        public string Uuid { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }
        public bool? Deleted { get; set; }
        public DateTime? Created { get; set; }
        public DateTime? LastChanged { get; set; }
        public string ParentUuid { get; set; }
        public string Shortname { get; set; }
        public string Name { get; set; }
        public string DisplayName { get; set; }
        public string SourceName { get; set; }
        public string CvrName { get; set; }
        public long? Cvr { get; set; }
        public long? Ean { get; set; }
        public bool? EanInherited { get; set; }
        public long? Senr { get; set; }
        public long? Pnr { get; set; }
        public string CostBearer { get; set; }
        public string OrgType { get; set; }
        public long? OrgTypeId { get; set; }
        public string LocalExtensions { get; set; }
        public string KeyWords { get; set; }
        public string OpeningHours { get; set; }
        public string Notes { get; set; }
        public string Email { get; set; }
        public int Id { get; set; }
        public ICollection<OrgUnitPhone> Phones { get; set; }
        public ICollection<OrgUnitPost> Addresses{ get; set; }
        public ICollection<OrgUnitKLEPrimary> KLEPrimary { get; set; }
        public ICollection<OrgUnitKLESecondary> KLESecondary { get; set; }
        public ICollection<Affiliation> Affiliations { get; set; }
        public ICollection<OrgUnitTag> Tags { get; set; }
        public OrgUnitManager Manager { get; set; }
        public ICollection<SubstituteOrgUnitAssignment> Substitutes { get; set; }
    }
}
