using System;

namespace sofd_core_safety_net.Services.SafetyNet.Model
{
    public class Employee
    {
        public int OpusId { get; set; }
        public string Cpr { get; set; }
        public string Firstname { get; set; }
        public string Surname { get; set; }
        public int OrgUnitLosId { get; set; }
        public bool IsManager { get; set; }
        public string Street { get; set; }
        public string StreetNumber { get; set; }
        public string PostalCode { get; set; }
        public string PhoneNumber { get; set; }
        public string PrimaryEmail { get; set; }
        public string PrimaryADUsername { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? StopDate { get; set; }
        public string Position { get; set; }
        public int ManagerLevel { get; set; }
        public int ManagerOpusId { get; set; }
        public bool IsAMR { get; set; }
        public bool IsHMU { get; set; }
        public bool IsOMU { get; set; }
        public bool IsLMU { get; set; }
        public bool IsDS { get; set; }
        public bool IsTR { get; set; }
        public bool IsFTR { get; set; }
    }
}
