using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using SofdCprIntegration.Model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace SofdCprIntegration.Controllers
{
    [ApiController]
    [Produces("application/json")]
    [Route("api/[controller]")]
    public class BadStateController : ControllerBase
    {
        private IConfiguration Configuration;
        private PersonContext personContext;

        public BadStateController(IConfiguration configuration, PersonContext personContext)
        {
            this.Configuration = configuration;
            this.personContext = personContext;
        }

        [EnableCors("AllowMyOrigin")]
        [HttpGet]
        public ActionResult<List<BadState>> Get()
        {
            return personContext.BadState.Where(s => 0 > DateTime.Compare(DateTime.Now, s.Tts.AddDays(30))).ToList();
        }
    }
}
