using System.Reflection;
using log4net;
using log4net.Appender;
using log4net.Layout;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;

namespace SofdCprIntegration
{
    public class Program
    {
        public static void Main(string[] args)
        {
            PatternLayout patternLayout = new PatternLayout();
            patternLayout.ConversionPattern = "%date - %-5level %logger - %message%newline";
            patternLayout.ActivateOptions();

            ConsoleAppender appender = new ConsoleAppender();
            appender.Layout = patternLayout;
            appender.ActivateOptions();

            var logRepository = (log4net.Repository.Hierarchy.Hierarchy) LogManager.GetRepository(Assembly.GetEntryAssembly());
            logRepository.Root.AddAppender(appender);

            logRepository.Root.Level = log4net.Core.Level.Info;
            logRepository.Configured = true;

            CreateWebHostBuilder(args).Build().Run();
        }

        public static IWebHostBuilder CreateWebHostBuilder(string[] args)
        {
            var config = new ConfigurationBuilder()
                .AddJsonFile("appsettings.json")
                .AddJsonFile("appsettings.development.json", true)
                .AddEnvironmentVariables()
                .Build();

            return WebHost.CreateDefaultBuilder(args)
                .UseUrls("http://*:5000")
                .UseConfiguration(config)
                .UseStartup<Startup>();
        }
    }
}
