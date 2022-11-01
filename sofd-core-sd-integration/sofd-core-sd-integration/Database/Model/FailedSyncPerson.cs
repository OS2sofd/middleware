using System;
using System.ComponentModel.DataAnnotations;

namespace sofd_core_sd_integration.Database.Model
{
    public class FailedSyncPerson
    {
        public FailedSyncPerson(string cpr, string errorMessage)
        {
            Cpr = cpr;
            ErrorMessage = errorMessage;
            FailedTimestamp = DateTime.Now;
        }
        [Key]
        public int Id { get; set; }
        public string Cpr { get; set; }
        public DateTime FailedTimestamp { get; set; }
        public string ErrorMessage { get; set; }
    }
}