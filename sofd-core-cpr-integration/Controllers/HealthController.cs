using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;

namespace SofdCprIntegration
{
    [ApiController]
    [Produces("application/json")]
    [Route("manage/health")]
    public class HealthController : ControllerBase
    {
        private IConfiguration Configuration;

        public HealthController(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        [HttpGet]
        public ActionResult Health()
        {
            return Ok();
        }

    }
}