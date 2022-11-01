using System.ComponentModel.DataAnnotations;

namespace sofd_core_sd_integration.Database.Model
{
    public class FullSyncPerson
    {
        [Key]
        public int Id { get; set; }
        
        public string Cpr { get; set; }
    }
}
