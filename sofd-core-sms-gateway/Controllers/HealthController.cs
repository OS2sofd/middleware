using Microsoft.AspNetCore.Mvc;

public class HealthController : Controller
{
    [HttpGet]
    [Route("manage/health")]
    public ActionResult Health()
    {
        return Ok();
    }

}