using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_kle_primary")]    
    public class OrgUnitKLEPrimary : KLE
    {
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }
    }
}
