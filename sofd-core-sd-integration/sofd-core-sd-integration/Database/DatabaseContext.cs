using Microsoft.EntityFrameworkCore;
using sofd_core_sd_integration.Database.Model;

namespace sofd_core_sd_integration.Database
{
    public class DatabaseContext : DbContext
    {
        public virtual DbSet<SynchronizeInfo> SynchronizeInfo { get; set; }
        public virtual DbSet<FullSyncPerson> FullSyncPersons { get; set; }
        public virtual DbSet<FailedSyncPerson> FailedSyncPersons { get; set; }
        public virtual DbSet<DBOrgUnit> DBOrgUnits { get; set; }
        public virtual DbSet<MQLog> MQLogs { get; set; }

        public DatabaseContext(DbContextOptions<DatabaseContext> options)
          : base(options)
        { }
    }
}