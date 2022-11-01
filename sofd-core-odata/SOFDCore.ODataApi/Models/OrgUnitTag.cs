using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_tag")]
    public class OrgUnitTag
    {
        [Key]
        public int RefId { get; set; }
        public string Tag { get; set; }
        public string CustomValue { get; set; }

        [ForeignKey("OrgUnit")]
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }
    }
}
