using System.Collections.Generic;

namespace DigitalIdentity.SD.Model
{
    public class SDOrgUnit
    {
        public string Uuid { get; set; }
        public string ParentUuid { get; set; }
        public string DepartmentIdentifier { get; set; }
        public string ParentDepartmentIdentifier { get; set; }        
        public string Name { get; set; }
        public bool IsManagerUnit { get; set; } = false;
        public int SubManagerCount { get; set; } = 0;
        public string NewParentUuid{ get; set; }
        public string Street { get; set; }
        public string PostalCode { get; set; }
        public string City { get; set; }
        public bool IncludeUnit { get; set; } = false;
        public string ManagerEmployeeId { get; set; }

        public string Phone;
        public bool IsFake { get; set; } = false;
    }
}
