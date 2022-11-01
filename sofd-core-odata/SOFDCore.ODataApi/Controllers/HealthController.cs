using Microsoft.AspNetCore.Mvc;

namespace SOFDCore.ODataApi.Controllers
{
    [Route("/manage/health")]
    public class HealthController : Controller
    {
        public IActionResult Health()
        {
            return Ok();
        }
    }
}
