using System.Collections.Generic;

namespace sofd_core_safety_net.Services.Sofd.Model
{
    public class OrgUnit
    {
        public string Master { get; set; }
        public string MasterId { get; set; }
        public string Uuid { get; set; }
        public string ParentUuid { get; set; }
        public string Name { get; set; }
        public bool Deleted { get; set; }
        public string Pnr { get; set; }
        public int Id { get; set; }
        public Manager Manager { get; set; }
        public List<OrgUnitPost> PostAddresses { get; set; }
    }
}
