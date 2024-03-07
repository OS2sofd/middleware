using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_substitute")]
    public class SubstituteOrgUnitAssignment
    {
        [Key]
        public long Id { get; set; }

        [ForeignKey("OrgUnit")]
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }
        
//      [ForeignKey("Substitute")]
        public string SubstituteUuid { get; set; }
        public string SubstituteName { get; set; }
//      public Person Substitute { get; set; }
        public string SubstituteContextName { get; set; }
        public string SubstituteContextIdentifier { get; set; }
        public string Type { get; set; }
        public DateTime Created { get; set; }
        public DateTime Changed { get; set; }
        public bool Inherited { get; set; }
    }
}