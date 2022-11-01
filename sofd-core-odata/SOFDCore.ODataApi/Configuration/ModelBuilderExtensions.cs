using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;
using System.Linq;

namespace SOFDCore.ODataApi.Configuration
{
    /// <summary>
    /// Entity Framework ModelBuilderExtensions
    /// </summary>
    public static class ModelBuilderExtensions
    {
        /// <summary>
        /// Converts camel case property names to underscore column names
        /// </summary>
        /// <param name="modelBuilder"></param>
        public static void SetUnderscoreColumnNameConvention(this ModelBuilder modelBuilder)
        {
            foreach (IMutableEntityType entity in modelBuilder.Model.GetEntityTypes())
            {
                foreach (var property in entity.GetProperties())
                {
                    property.SetColumnName(GetUnderscoreCase(property.Name));
                }
            }
        }

        private static string GetUnderscoreCase(string str)
        {
            return string.Concat(str.Select((x, i) => i > 0 && char.IsUpper(x) ? "_" + x.ToString() : x.ToString())).ToLower();
        }
    }
}
