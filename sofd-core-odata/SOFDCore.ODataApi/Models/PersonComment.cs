using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_person_comment")]
    public class PersonComment
    {
        [Key]
        public long Id { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }

        public string UserName { get; set; }
        public DateTime Timestamp { get; set; }
        public string Comment { get; set; }
    }
}