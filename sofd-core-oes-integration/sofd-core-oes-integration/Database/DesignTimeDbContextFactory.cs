using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;
using Pomelo.EntityFrameworkCore.MySql.Infrastructure;
using sofd_core_oes_integration.Settings;
using System.IO;

namespace sofd_core_oes_integration.Database
{
    public class DesignTimeDbContextFactory : IDesignTimeDbContextFactory<DatabaseContext>
    {
        public DatabaseContext CreateDbContext(string[] args)
        {
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json")
                .AddJsonFile("appsettings.development.json")
                .Build();
            var appSettings = new AppSettings();
            configuration.Bind(appSettings);

            var connectionString = configuration.GetConnectionString("sqlserver");

            var builder = new DbContextOptionsBuilder<DatabaseContext>();
            builder.UseMySql($"Server={appSettings.DatabaseSettings.Server};Database={appSettings.DatabaseSettings.Database};Uid={appSettings.DatabaseSettings.User};Pwd={appSettings.DatabaseSettings.Password};",
                    mysqlOptions => mysqlOptions.ServerVersion(new System.Version(5, 6, 30), ServerType.MySql));

            return new DatabaseContext(builder.Options);
        }
    }
}
