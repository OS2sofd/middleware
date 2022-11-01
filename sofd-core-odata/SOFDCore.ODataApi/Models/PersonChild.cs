using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_child")]
    public class PersonChild
    {
        [Key]
        public int RefId { get; set; }
        public string Cpr { get; set; }
        public string Name { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }
    }

}
