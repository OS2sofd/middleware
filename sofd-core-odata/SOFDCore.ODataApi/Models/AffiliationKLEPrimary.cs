using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_affiliation_kle_primary")]
    public class AffiliationKLEPrimary : KLE
    {
        public string AffiliationUuid { get; set; }
    }

}
