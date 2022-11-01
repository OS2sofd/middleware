using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace sofd_core_sd_integration.Database.Model
{
    public class MQLog
    {
        [Key]
        public int Id { get; set; }
        public string MessageId{ get; set; }
        public DateTime Timestamp { get; set; }
        public string Operation { get; set; }
        public bool IsSent { get; set; } = false;
        public string Message { get; set; }
        public string OrgUnitUuid { get; set; }
    }
}