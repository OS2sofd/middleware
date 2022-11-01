using System.ComponentModel.DataAnnotations;

namespace SOFDCore.ODataApi.Models
{
    public abstract class KLE
    {
        [Key]
        public string Code { get; set; }
        public string Name { get; set; }
        public bool? Active { get; set; }        
    }
}
