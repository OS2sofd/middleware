using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_substitute_orgunit")]
    public class SubstituteOrgUnit
    {
        [Key]
        public long Id { get; set; }

        [ForeignKey("SubstituteAssignment")]
        public long SubstituteAssignmentId { get; set; }
        public SubstituteAssignment SubstituteAssignment { get; set; }

        public string OrgunitUuid { get; set; }
    }
}