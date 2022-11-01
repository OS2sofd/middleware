using System.ComponentModel.DataAnnotations.Schema;

namespace SOFDCore.ODataApi.Models
{
    [Table("view_odata_orgunit_manager")]
    public class OrgUnitManager
    {
        public int Id { get; set; }
        public bool Inherited { get; set; }
        public string Name { get; set; }

        [ForeignKey("OrgUnit")]
        public string OrgUnitUuid { get; set; }
        public OrgUnit OrgUnit { get; set; }

        [ForeignKey("Person")]
        public string PersonUuid { get; set; }
        public Person Person { get; set; }        
    }
}
