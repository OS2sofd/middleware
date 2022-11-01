using Microsoft.Extensions.Logging;
using Microsoft.Extensions.DependencyInjection;
using System;
using sofd_core_sd_integration;

namespace DigitalIdentity
{
    public abstract class BaseClass<T> where T : class
    {
        protected readonly ILogger logger;
        protected readonly AppSettings appSettings;
        public BaseClass(IServiceProvider sp)
        {
            logger = sp.GetService<ILogger<T>>();
            appSettings = sp.GetService<AppSettings>();
        } 
    }
}