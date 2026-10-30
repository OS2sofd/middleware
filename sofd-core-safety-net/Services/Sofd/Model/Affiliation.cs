using System;
using System.Collections.Generic;

namespace sofd_core_safety_net.Services.Sofd.Model
{
    public class Affiliation
    {
        public DateTime? StartDate { get;  set; }
        public DateTime? StopDate { get;  set; }
        public string PositionName { get;  set; }
        public string OrgunitUuid { get;  set; }
        public string Master { get;  set; }
        public string EmployeeId { get; set; }
        public bool Prime { get;  set; }
        public bool Deleted { get;  set; }
        public string OrgUnitUuid { get;  set; }
        public List<string> Functions { get; set; }
    }
}
