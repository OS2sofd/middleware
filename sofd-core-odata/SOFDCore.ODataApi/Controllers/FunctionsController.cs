using Microsoft.AspNetCore.Mvc;
using SOFDCore.ODataApi.Database;
using System;
using Microsoft.Extensions.DependencyInjection;
using System.Linq;
using Microsoft.AspNet.OData;
using SOFDCore.ODataApi.Models;
using SOFDCore.ODataApi.Filters;

namespace SOFDCore.ODataApi.Controllers
{
    [ServiceFilter(typeof(AuditlogFilter))]
    public class FunctionsController : ODataController
    {

        private readonly SOFDContext sofdContext;

        public FunctionsController(IServiceProvider provider)
        {
            sofdContext = provider.GetService<SOFDContext>();
        }

        public IQueryable<Function> Get()
        {
            return sofdContext.Functions;
        }

        public IQueryable<Function> Get(long key)
        {
            return sofdContext.Functions.Where(f => f.Id == key);
        }
    }
}
