using System;
using System.ComponentModel.DataAnnotations;

namespace sofd_core_oes_integration.Database.Model
{
    public class SynchronizeInfo
    {
        [Key]
        public int Id { get; set; }
        public DateTime PersonsLastSync { get; set; }
    }
}