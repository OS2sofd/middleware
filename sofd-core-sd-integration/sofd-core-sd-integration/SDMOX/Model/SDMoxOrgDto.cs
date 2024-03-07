namespace DigitalIdentity.SDMOX.Model
{
    public class SDMoxOrgDto
    {
        public string Name { get; set; }
        public string Level { get; set; }
        public string Uuid { get; set; }
        public string ParentUuid { get; set; }
        public string Pnr { get; set; }
        public string Phone { get; set; }
        public string Address { get; set; }
        public string PostalCode { get; set; }
        public string City { get; set; }
        public string Code { get; set; }

        public SDMoxOrgDto GetDepartmentCopy(string Uuid, string ParentUuid)
        {
            var department = (SDMoxOrgDto)this.MemberwiseClone();
            department.Uuid = Uuid;
            department.ParentUuid = ParentUuid;
            department.Level = "Afdelings-niveau";
            return department;
        }
    }
}