using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace SOFDCore.ODataApi.Models
{
    [Table("client")]
    public class Client
    {
        [Key]
        public int Id { get; set; }
        public string Name { get; set; }
        public string ApiKey { get; set; }
        public string AccessRole { get; set; }
        public string TlsVersion { get; set; }
        public ICollection<AccessField> AccessFields { get; set; }
        public ICollection<IpRange> IpRanges { get; set; }
    }
}