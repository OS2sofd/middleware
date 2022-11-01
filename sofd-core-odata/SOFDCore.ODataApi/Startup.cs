using Microsoft.AspNet.OData;
using Microsoft.AspNet.OData.Extensions;
using Microsoft.AspNet.OData.Routing;
using Microsoft.AspNet.OData.Routing.Conventions;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.HttpOverrides;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Authorization;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.OData;
using Pomelo.EntityFrameworkCore.MySql.Infrastructure;
using SOFDCore.ODataApi.Auth;
using SOFDCore.ODataApi.Configuration;
using SOFDCore.ODataApi.Database;
using SOFDCore.ODataApi.Filters;
using System.Collections.Generic;
using System.Linq;
using System.Net;

namespace SOFDCore.ODataApi
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        public void ConfigureServices(IServiceCollection services)
        {
            services.AddHttpContextAccessor();
            services.AddLogging();
            services.AddDbContext<SOFDContext>(options => options.UseMySql(
                "Server=" + Configuration["SOFDDatabase:Server"] +
                ";Database=" + Configuration["SOFDDatabase:Database"] +
                ";Uid=" + Configuration["SOFDDatabase:Uid"] +
                ";Pwd=" + Configuration["SOFDDatabase:Pwd"] + ";",
                    mysqlOptions => mysqlOptions.ServerVersion(new System.Version(5, 6, 30), ServerType.MySql)));

            services.AddDbContext<AuditContext>(options => options.UseMySql(
                "Server=" + Configuration["AuditDatabase:Server"] +
                ";Database=" + Configuration["AuditDatabase:Database"] +
                ";Uid=" + Configuration["AuditDatabase:Uid"] +
                ";Pwd=" + Configuration["AuditDatabase:Pwd"] + ";",
                    mysqlOptions => mysqlOptions.ServerVersion(new System.Version(5, 6, 30), ServerType.MySql)));
            services.AddSingleton<AuditlogFilter>();


            services.AddAuthentication(options =>
            {
                options.DefaultAuthenticateScheme = ApiKeyAuthenticationOptions.DefaultScheme;
                options.DefaultChallengeScheme = ApiKeyAuthenticationOptions.DefaultScheme;
            }).AddApiKeySupport(options => { });

            services.AddMvc(options => {
                options.EnableEndpointRouting = false;
                options.Filters.Add(new AuthorizeFilter());
                options.Filters.Add(new EnableQueryAttribute() { MaxExpansionDepth = Configuration.GetValue<int>("OData:MaxExpansionDepth") });
                options.MaxIAsyncEnumerableBufferLimit = 50000;
            }).SetCompatibilityVersion(CompatibilityVersion.Version_3_0);
            services.AddOData();
            services.AddScoped<LimitedReadAccessODataModelBuilder>();
        }

        public void Configure(IApplicationBuilder app, LimitedReadAccessODataModelBuilder modelBuilder )
        {
            app.UseAuthentication();

            app.UseMvc(routeBuilder =>
            {
                routeBuilder.EnableDependencyInjection();
                routeBuilder.Expand().Select().Filter().Count().OrderBy().MaxTop(null);
                routeBuilder.MapODataServiceRoute("odata", "odata", containerBuilder =>
                {
                    containerBuilder.AddService(Microsoft.OData.ServiceLifetime.Scoped, sp => modelBuilder.GetEdmModel());
                    containerBuilder.AddService<IODataPathHandler>(Microsoft.OData.ServiceLifetime.Singleton, sp => new DefaultODataPathHandler());
                    containerBuilder.AddService<IEnumerable<IODataRoutingConvention>>(Microsoft.OData.ServiceLifetime.Singleton, sp => ODataRoutingConventions.CreateDefaultWithAttributeRouting("odata", routeBuilder));
                });
            });
        }
    }
}
