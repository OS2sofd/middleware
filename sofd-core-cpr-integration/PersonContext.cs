using Microsoft.EntityFrameworkCore;
using SofdCprIntegration.Controllers;

namespace SofdCprIntegration
{
    public class PersonContext : DbContext
    {
        public DbSet<Person> Person { get; set; }
        public DbSet<LastSync> LastSync { get; set; }

        public PersonContext() {
            ; // used when running without a database connection
        }

        public PersonContext(DbContextOptions<PersonContext> options) : base(options)
        {

        }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<Person>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.AddressProtected).HasConversion<int>();
                entity.HasIndex(p => new { p.Cpr }).IsUnique();
                entity.HasMany(p => p.Children).WithOne(c => c.Parent).HasForeignKey(c => c.ParentId).OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<LastSync>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.LastSyncDate);
            });
        }
    }
}