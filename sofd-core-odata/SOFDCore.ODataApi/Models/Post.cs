using System.ComponentModel.DataAnnotations;

namespace SOFDCore.ODataApi.Models
{
    public abstract class Post
    {
        [Key]
        public long Id { get; set; }
        public bool? Prime { get; set; }
        public string Street { get; set; }
        public string Localname { get; set; }
        public string PostalCode { get; set; }
        public string City { get; set; }
        public string Country { get; set; }
        public bool? AddressProtected { get; set; }
        public string Master { get; set; }
        public string MasterId { get; set; }
    }
}
