using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_disabled_user")]
    public partial class DisabledUser
    {
        [Key]
        public string Uuid { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }
        public string UserId { get; set; }
        public string LocalExtensions { get; set; }
        public string UserType { get; set; }
        public bool Prime { get; set; }
        public string EmployeeId { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }
    }
}
