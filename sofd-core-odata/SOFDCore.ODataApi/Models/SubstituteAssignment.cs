using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_substitute")]
    public class SubstituteAssignment
    {
        [Key]
        public long Id { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }
        
//        [ForeignKey("Substitute")]
        public string SubstituteUuid { get; set; }
//        public Person Substitute { get; set; }
        public string SubstituteContextName { get; set; }
        public string SubstituteContextIdentifier { get; set; }
        public bool SubstituteContextSupportsConstraints { get; set; }
        public ICollection<SubstituteOrgUnit> OrgUnitConstraints { get; set; }        
        public DateTime Created { get; set; }
        public DateTime Changed { get; set; }
    }
}