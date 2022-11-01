using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_affiliation_kle_secondary")]
    public class AffiliationKLESecondary : KLE
    {
        public string AffiliationUuid { get; set; }
        public Affiliation Affiliation { get; set; }
    }

}