using Amazon;
using Amazon.CloudWatchLogs;
using DigitalIdentity.CloudWatch;
using DigitalIdentity.Email;
using DigitalIdentity.S3;
using DigitalIdentity.SOFD;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Pomelo.EntityFrameworkCore.MySql.Infrastructure;
using Serilog;
using Serilog.Formatting.Display;
using Serilog.Sinks.AwsCloudWatch;
using sofd_core_oes_integration.Database;
using sofd_core_oes_integration.OES;
using sofd_core_oes_integration.Settings;
using System;
using System.IO;
using System.Threading;

namespace sofd_core_oes_integration
{
    class Program
    {
        static void Main(string[] args)
        {
            var serviceProvider = GetServiceProvider();
            var syncService = new SyncService(serviceProvider);
            syncService.Synchronize();
            Log.CloseAndFlush();
            Thread.Sleep(60000); // give serilog time to flush the messages
        }

        static IServiceProvider GetServiceProvider()
        {
            var services = new ServiceCollection();

            var configuration = new ConfigurationBuilder()
                .AddJsonFile("appsettings.json")
                .AddEnvironmentVariables()
                // add optional development config without copying it to output dir
                .AddJsonFile(Path.Combine(Directory.GetParent(AppDomain.CurrentDomain.BaseDirectory)?.Parent?.Parent?.Parent?.FullName ?? "", "appsettings.development.json"), true)
                .AddJsonFile("/settings/appsettings.production.json", true)
                .Build();
            var appSettings = new AppSettings();
            configuration.Bind(appSettings);
            services.AddSingleton(appSettings);

            services.AddDbContext<DatabaseContext>(
                options => options.UseMySql($"Server={appSettings.DatabaseSettings.Server};Database={appSettings.DatabaseSettings.Database};Uid={appSettings.DatabaseSettings.User};Pwd={appSettings.DatabaseSettings.Password};SslMode=none;",
                    mysqlOptions => mysqlOptions.ServerVersion(new System.Version(5, 6, 30), ServerType.MySql)));

            var logger = new LoggerConfiguration().ReadFrom.Configuration(configuration);
            // cloudwatch enabled will only work on aws ec2 servers
            // (requires further configuration of credentials)
            if (appSettings.CloudWatchSettings.Enabled)
            {
                var region = RegionEndpoint.GetBySystemName(appSettings.CloudWatchSettings.Region);
                var client = new AmazonCloudWatchLogsClient(region);
                var textFormatter = new MessageTemplateTextFormatter("{Timestamp:yyyy-MM-dd HH:mm:ss} {Level:u} {SourceContext} {Message:lj}{NewLine}{Exception}", null);
                logger.WriteTo.AmazonCloudWatch(
                    appSettings.CloudWatchSettings.LogGroup,
                    new LogStreamNameProvider(appSettings.CloudWatchSettings.LogStream),
                    textFormatter: textFormatter,
                    cloudWatchClient: client);
            }
            Serilog.Debugging.SelfLog.Enable(Console.Error);
            services.AddLogging(builder => builder.AddSerilog(logger.CreateLogger()));

            services.AddSingleton<SyncService>();
            services.AddSingleton<SOFDService>();
            services.AddSingleton<OESService>();
            services.AddSingleton<EmailService>();
            services.AddSingleton<S3Service>();

            return services.BuildServiceProvider();
        }
    }
}