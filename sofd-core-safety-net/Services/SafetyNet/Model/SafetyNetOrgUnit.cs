namespace sofd_core_safety_net.SafetyNet.Model
{
    public class SafetyNetOrgUnit
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public int ParentId { get; set; }
        public string Pnr { get; set; }
        public string TopOrgUnitName { get; set; }
    }
}
