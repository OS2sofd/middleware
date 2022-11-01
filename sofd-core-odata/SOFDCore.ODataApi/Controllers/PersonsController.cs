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
    public class PersonsController : ODataController
    {

        private readonly SOFDContext sofdContext;

        public PersonsController(IServiceProvider provider)
        {
            sofdContext = provider.GetService<SOFDContext>();

        }

        public IQueryable<Person> Get()
        {
            return sofdContext.Persons;
        }

        public IQueryable<Person> Get(string key)
        {
            return sofdContext.Persons.Where(p => p.Uuid == key);
        }

        [HttpGet]
        public OkObjectResult DownloadPhoto(string key)
        {
            var result = sofdContext.Photos.Where(p => p.PersonUuid == key).Select(p => p.Data).FirstOrDefault();
            return Ok(result);
        }

    }
}
