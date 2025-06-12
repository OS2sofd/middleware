using System;
using System.Collections.Generic;

namespace sofd_core_safety_net.Services.Sofd.Model
{
    public class Person
    {
        public string Uuid { get; set; }
        public string Cpr { get; set; }
        public bool Deleted { get; set; }
        public string Master { get; set; }
        public string Firstname { get; set; }
        public string Surname { get; set; }
        public string ChosenName { get; set; }
        public DateTime? FirstEmploymentDate { get; set; }
        public Post RegisteredPostAddress { get; set; }
        public List<Affiliation> Affiliations { get; set; }
        public List<User> Users { get; set; }
        public List<Phone> Phones { get; set; }
    }
}
