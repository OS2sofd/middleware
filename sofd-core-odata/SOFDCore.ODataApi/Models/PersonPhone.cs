using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_phone")]    
    public class PersonPhone : Phone
    {
        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }
    }
}