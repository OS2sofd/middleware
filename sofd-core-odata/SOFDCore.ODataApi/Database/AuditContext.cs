using Microsoft.EntityFrameworkCore;
using SOFDCore.ODataApi.Configuration;
using SOFDCore.ODataApi.Models;

namespace SOFDCore.ODataApi.Database
{
    public class AuditContext : DbContext
    {
        public virtual DbSet<SecurityLog> SecurityLogs { get; set; }
        public virtual DbSet<Client> Clients { get; set; }

        public AuditContext(DbContextOptions<AuditContext> options)
          : base(options)
        { }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.SetUnderscoreColumnNameConvention();
        }
    }
}
