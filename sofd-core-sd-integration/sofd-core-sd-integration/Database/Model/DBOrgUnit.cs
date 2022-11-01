using DigitalIdentity.Utility;
using Newtonsoft.Json;
using System;
using System.ComponentModel.DataAnnotations;

namespace sofd_core_sd_integration.Database.Model
{
    public class DBOrgUnit : ChangeTrackable
    {
        [Key]
        public int Id { get; set; }
        public string SofdUuid { get; set; }
        public string VirtualUuid { get; set; }
        // ignoring this so that it will not be changetracked because these changes are checked explicitly
        [JsonIgnore]
        public string ParentVirtualUuid { get; set; }
        public string Name { get; set; }
        public int Level { get; set; }
        public string Street { get; set; }
        public string PostalCode { get; set; }
        public string City { get; set; }
        public string Phone { get; set; }
        public string PNumber { get; set; }
        public DateTime? Created { get; set; }
        [JsonIgnore]
        public DateTime? Changed { get; set; }
        public DateTime? Deleted { get; set; }
    }
}
