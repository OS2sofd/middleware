namespace SOFDCore.ODataApi.Models
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    /// <summary>
    /// Represents a function.
    /// </summary>
    [Table("view_odata_function_assignment")]
    public class FunctionAssignment
    {
        [Key]
        public long Id { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? StopDate { get; set; }
        public Affiliation Affiliation { get; set; }
        public List<FunctionAssignmentValue> Values { get; set; }
    }
}