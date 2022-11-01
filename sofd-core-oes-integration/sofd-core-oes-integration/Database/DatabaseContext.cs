using Microsoft.EntityFrameworkCore;
using sofd_core_oes_integration.Database.Model;

namespace sofd_core_oes_integration.Database
{
    public class DatabaseContext : DbContext
    {
        public virtual DbSet<SynchronizeInfo> SynchronizeInfo { get; set; }
        public virtual DbSet<DBOrgUnit> OrgUnits { get; set; }

        public DatabaseContext(DbContextOptions<DatabaseContext> options)
          : base(options)
        { }
    }
}