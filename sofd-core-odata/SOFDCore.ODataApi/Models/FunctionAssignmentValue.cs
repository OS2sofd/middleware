namespace SOFDCore.ODataApi.Models
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    /// <summary>
    /// Represents a function.
    /// </summary>
    [Table("view_odata_function_assignment_value")]
    public class FunctionAssignmentValue
    {
        [Key]
        public long Id { get; set; }
        public string Name { get; set; }
        public string Value { get; set; }

    }
}