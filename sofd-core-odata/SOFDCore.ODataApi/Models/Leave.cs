using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_leave")]
    public class Leave
    {
        [Key]
        public long Id { get; set; }
        public string PersonUuid { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? StopDate { get; set; }        
        public string Reason { get; set; }
        public string ReasonText { get; set; }
        public bool? DisableAccountOrders { get; set; }
        public bool? ExpireAccounts { get; set; }
    }
}