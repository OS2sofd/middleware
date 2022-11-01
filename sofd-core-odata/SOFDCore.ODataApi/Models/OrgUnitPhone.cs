using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_phone")]    
    public class OrgUnitPhone : Phone
    {
        [ForeignKey("OrgUnit")]
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }
    }
}
