using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_affiliation_function")]
    public class AffiliationFunction
    {
        [Key]
        public int Id { get; set; }
        public string Function { get; set; }

        [ForeignKey("Affiliation")]
        public string AffiliationUuid { get; set; }
        public Affiliation Affiliation { get; set; }
    }
}
