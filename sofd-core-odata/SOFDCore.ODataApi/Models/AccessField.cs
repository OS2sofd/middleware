using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace SOFDCore.ODataApi.Models
{
    [Table("access_field")]
    public class AccessField
    {
        [Key]
        public int Id { get; set; }
        public string Entity { get; set; }
        public string Field { get; set; }

        [ForeignKey("Client")]
        public int ClientId { get; set; }
        public Client Client { get; set; }
    }
}
