namespace SOFDCore.ODataApi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    /// <summary>
    /// Represents a function.
    /// </summary>
    [Table("view_odata_function_facet")]
    public class FunctionFacet
    {
        [Key]
        public long Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
    }
}