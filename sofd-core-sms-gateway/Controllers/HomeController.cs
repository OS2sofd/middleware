using System;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using SofdSmsGateway;

public class HomeController : Controller
{
    [HttpGet]
    [Route("/")]
    public ContentResult Index()
    {
        return Content("An API sending SMS through various providers.");
    }
}