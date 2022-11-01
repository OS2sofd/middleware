using System;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("security_log")]
    public class SecurityLog
    {
        public int Id { get; set; }
        public DateTime timestamp { get; set; }
        public int ClientId { get; set; }
        public string Clientname { get; set; }
        public string Method { get; set; }
        public string Request { get; set; }
        public string IpAddress { get; set; }
    }
}
