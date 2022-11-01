using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_photo")]
    public class Photo
    {
        [Key]
        public long Id { get; set; }
        public string PersonUuid { get; set; }
        public DateTime LastChanged { get; set; }
        public byte[] Data { get; set; }
        public long Checksum { get; set; }
        public string Format { get; set; }

    }
}