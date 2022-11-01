using Microsoft.AspNet.OData;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.DependencyInjection;
using SOFDCore.ODataApi.Database;
using SOFDCore.ODataApi.Filters;
using SOFDCore.ODataApi.Models;
using System;
using System.Linq;

namespace SOFDCore.ODataApi.Controllers
{
    [ServiceFilter(typeof(AuditlogFilter))]
    public class OrgUnitsController : ODataController
    {

        private readonly SOFDContext sofdContext;

        public OrgUnitsController(IServiceProvider provider)
        {
            sofdContext = provider.GetService<SOFDContext>();

        }

        public IQueryable<OrgUnit> Get()
        {
            return sofdContext.OrgUnits;
        }

        public IQueryable<OrgUnit> Get(string key)
        {
            return sofdContext.OrgUnits.Where(o => o.Uuid == key);
        }

    }

}
