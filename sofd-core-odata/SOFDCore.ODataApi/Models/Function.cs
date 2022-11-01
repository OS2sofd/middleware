namespace SOFDCore.ODataApi.Models
{
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    /// <summary>
    /// Represents a function.
    /// </summary>
    [Table("view_odata_function")]
    public class Function
    {
        [Key]
        public long Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public ICollection<FunctionAssignment> Assignments { get; set; }
    }
}