using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_email")]    
    public class OrgUnitEmail
    {
        [Key]
        public long Id { get; set; }
        public bool? Prime { get; set; }
        public string Email { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }

        [ForeignKey("OrgUnit")]
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }
    }
}