using Microsoft.EntityFrameworkCore;
using SOFDCore.ODataApi.Configuration;
using SOFDCore.ODataApi.Controllers;
using SOFDCore.ODataApi.Models;
using System.Linq;

namespace SOFDCore.ODataApi.Database
{
    public class SOFDContext : DbContext
    {
        public virtual DbSet<Person> Persons { get; set; }
        public virtual DbSet<OrgUnit> OrgUnits { get; set; }
        public virtual DbSet<Client> Clients { get; set; }
        public virtual DbSet<Photo> Photos { get; set; }
        public virtual DbSet<AccessField> AccessFields { get; set; }
        public virtual DbSet<Function> Functions { get; set; }
        public virtual DbSet<IpRange> IpRanges { get; set; }

        public SOFDContext(DbContextOptions<SOFDContext> options) : base(options)
        { }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.SetUnderscoreColumnNameConvention();

            // modelBuilder.Entity<Person>().HasMany<SubstituteAssignment>().WithOne("Person");

            modelBuilder.Entity<Person>().Ignore(p => p.CalculatedName);
        }
    }
}
