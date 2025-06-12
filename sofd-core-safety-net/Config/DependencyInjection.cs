using Amazon;
using Amazon.CloudWatchLogs;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Serilog;
using Serilog.Formatting.Display;
using Serilog.Sinks.AwsCloudWatch;
using sofd_core_safety_net.Amazon;
using sofd_core_safety_net.Services.Sftp;
using sofd_core_safety_net.Services.SafetyNet;
using sofd_core_safety_net.Services.Sofd;
using System;
using System.IO;

namespace sofd_core_safety_net.Config
{
    public static class DependencyInjection
    {
        public static IServiceCollection AddDependencies(this IServiceCollection services)
        {
            var configuration = new ConfigurationBuilder()  
                .AddJsonFile("appsettings.json", optional: false)
                .AddEnvironmentVariables()
                .AddJsonFile(Directory.GetParent(Directory.GetCurrentDirectory())?.Parent?.Parent?.FullName + "/appsettings.development.json", optional: true)
                .Build();

            // Bind settings
            var settings = new Settings();
            configuration.Bind(settings);
            services.AddSingleton(settings);

            // logger
            var logger = new LoggerConfiguration()
                .ReadFrom.Configuration(configuration);

            // cloudwatch enabled will only work on aws ec2 servers
            // (requires further configuration of credentials)
            if (settings.AmazonCloudWatch.Enabled)
            {
                var region = RegionEndpoint.GetBySystemName(settings.AmazonCloudWatch.Region);
                var client = new AmazonCloudWatchLogsClient(region);
                var textFormatter = new MessageTemplateTextFormatter("{Timestamp:yyyy-MM-dd HH:mm:ss} {Level:u} {SourceContext} {Message:lj}{NewLine}{Exception}", null);
                logger.WriteTo.AmazonCloudWatch(
                    settings.AmazonCloudWatch.LogGroup,
                    new LogStreamNameProvider(settings.AmazonCloudWatch.LogStream),
                    textFormatter: textFormatter,
                    cloudWatchClient: client);
            }
            Serilog.Debugging.SelfLog.Enable(Console.Error);
            Log.Logger = logger.CreateLogger();

            // Add other required services
            services.AddSingleton<SftpService>();
            services.AddSingleton<SofdService>();
            services.AddSingleton<SafetyNetService>();

            return services;
        }
    }
}
