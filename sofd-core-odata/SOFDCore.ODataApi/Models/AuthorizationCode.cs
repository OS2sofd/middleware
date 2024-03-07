using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_authorization_code")]    
    public class AuthorizationCode
    {
        [Key]
        public long Id { get; set; }
        public bool Prime { get; set; }
        public string Code { get; set; }
        public string Name { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }
    }
}