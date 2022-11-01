using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace SofdCprIntegration.Controllers
{
    public class Person
    {
        public long Id { get; set; }

        public string Firstname { get; set; }

        public string Lastname { get; set; }

        public string Street { get; set; }

        public string Localname { get; set; }

        public string PostalCode { get; set; }

        public string City { get; set; }

        public string Country { get; set; }

        public bool AddressProtected { get; set; }

        [MaxLength(10)]
        public string Cpr { get; set; }

        public DateTime Created { get; set; }

        public DateTime LastUsed { get; set; }

        public ICollection<Child> Children { get; set; } = new List<Child>();
        public bool IsDead { get; set; }
        public bool Disenfranchised { get; set; }
    }
}