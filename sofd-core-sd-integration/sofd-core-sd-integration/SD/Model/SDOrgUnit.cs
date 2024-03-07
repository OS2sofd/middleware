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
    }
}
