using System;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Quartz;
using Quartz.Impl;
using Quartz.Spi;

[assembly: ApiController]
namespace SofdCprIntegration
{
    public class Startup
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        public IConfiguration Configuration { get; }

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMvc(options => options.EnableEndpointRouting = false).SetCompatibilityVersion(CompatibilityVersion.Version_3_0);

            string connectionString = Configuration.GetConnectionString("mysql");
            if (!string.IsNullOrEmpty(connectionString))
            {
                services.AddDbContext<PersonContext>(options => options.UseMySql(connectionString,ServerVersion.AutoDetect(connectionString)));

                // Add Quartz services
                services.AddSingleton<IJobFactory, SingletonJobFactory>();
                services.AddSingleton<ISchedulerFactory, StdSchedulerFactory>();

                // Add our FTP job
                services.AddSingleton<SyncJob>();
                services.AddSingleton(new JobSchedule(jobType: typeof(SyncJob), cronExpression: Configuration["SFTPService:cron"]));

                // Add HostedService (background task)
                services.AddHostedService<QuartzHostedService>();
            }
            else
            {
                services.AddSingleton<PersonContext>();
            }
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            string connectionString = Configuration.GetConnectionString("mysql");
            if (!string.IsNullOrEmpty(connectionString))
            {
                InitializeDatabase(app);
            }
            app.UseCors("AllowMyOrigin");
            app.UseMvc();
        }

        private void InitializeDatabase(IApplicationBuilder app)
        {
            using (var scope = app.ApplicationServices.GetService<IServiceScopeFactory>().CreateScope())
            {
                scope.ServiceProvider.GetRequiredService<PersonContext>().Database.Migrate();
            }
        }

    }
}
