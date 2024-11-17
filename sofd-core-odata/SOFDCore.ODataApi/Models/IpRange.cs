using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace SOFDCore.ODataApi.Models
{
    [Table("known_networks")]
    public class IpRange
    {
        [Key]
        public int Id { get; set; }
        public string Ip { get; set; }

        [ForeignKey("Client")]
        public int ClientId { get; set; }
        public Client Client { get; set; }
    }
}