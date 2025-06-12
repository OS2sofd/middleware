using Microsoft.Extensions.DependencyInjection;
using Quartz;
using sofd_core_safety_net.Jobs;

namespace sofd_core_safety_net.Config
{
    public static class QuartzConfiguration
    {
        public static IServiceCollection AddQuartzJobs(this IServiceCollection services)
        {
            var settings = services.BuildServiceProvider().GetService<Settings>();

            // We use Quartz to schedule jobs
            services.AddQuartz(q =>
            {
                q.UseMicrosoftDependencyInjectionJobFactory();

                // Add SyncJob
                var syncJobKey = new JobKey("SyncJob");
                q.AddJob<GenerateFilesJob>(syncJobKey, j => j.WithDescription(syncJobKey.Name));

                // add initial trigger
                q.AddTrigger(t => t
                    .WithIdentity("Startup Trigger")
                    .ForJob(syncJobKey)
                    .StartNow()
                );

                // Add scheduled trigger
                q.AddTrigger(t => t
                    .WithIdentity("FullSync Cron Trigger")
                    .ForJob(syncJobKey)
                    .WithCronSchedule(settings.JobSettings.GenerateFilesCron)
                );
            });

            return services;
        }
    }
}