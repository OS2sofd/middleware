using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace SofdCprIntegration.Controllers
{
    public class Child
    {
        public long Id { get; set; }

        [MaxLength(10)]
        public string Cpr { get; set; }

        [JsonIgnore]
        public Person Parent { get; set; }

        public long? ParentId { get; set; }
    }
}