using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using sofd_core_oes_integration.Settings;
using System;

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