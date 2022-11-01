using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    public abstract class Phone
    {
        [Key]
        public long Id { get; set; }
        public bool? Prime { get; set; }
        public bool? TypePrime { get; set; }
        public string PhoneNumber { get; set; }
        public string PhoneType { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }
        public string Notes { get; set; }
        public string Visibility { get; set; }
        public string FunctionType { get; set; }
    }
}
