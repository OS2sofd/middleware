using Microsoft.EntityFrameworkCore;
using SofdCprIntegration.Controllers;
using SofdCprIntegration.Model;

namespace SofdCprIntegration
{
    public class PersonContext : DbContext
    {
        public DbSet<Person> Person { get; set; }
        public DbSet<LastSync> LastSync { get; set; }
        public DbSet<BadState> BadState { get; set; }

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

            modelBuilder.Entity<BadState>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.HasIndex(e => new { e.Cpr }).IsUnique();
            });
        }
    }
}