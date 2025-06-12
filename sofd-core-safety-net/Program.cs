using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Configuration;
using Quartz;
using Serilog;
using sofd_core_safety_net.Config;

namespace sofd_core_safety_net
{
    public class Program
    {
        public static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .UseSerilog()
                .ConfigureServices((hostContext, services) =>
                {
                    // Add dependecies using extension method in Config/DependencyInjection.cs
                    // The CreateDefaultBuilder automatically loads a Configuration based on appsettings.json and appsettings.Development.json into the hostContext.Configuration
                    services.AddDependencies();

                    // Add quartz jobs using extension method in Config/QuartzConfiguraiton.cs
                    services.AddQuartzJobs();

                    // Quartz has a nice convenience method for hosting the main program
                    services.AddQuartzServer(options =>
                    {
                        // when shutting down we want jobs to complete gracefully
                        options.WaitForJobsToComplete = true;
                    });
                });

    }
}
