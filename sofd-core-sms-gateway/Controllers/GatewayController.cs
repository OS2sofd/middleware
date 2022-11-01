using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace SofdSmsGateway
{
    [ApiController]
    [Produces("application/json")]
    [Route("api/[controller]")]
    public class GatewayController : ControllerBase
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private IConfiguration Configuration;
        private readonly ILogger Logger;

        public GatewayController(IConfiguration configuration, ILogger<GatewayController> logger)
        {
            Configuration = configuration;
            Logger = logger;
        }

        [HttpPost]
        public ActionResult<string> SendMessage(Message message)
        {
            ISMSGateway gateway = null;

            if (Configuration.GetSection(message.cvr) != null)
            {
                switch (Configuration.GetSection(message.cvr).GetValue<string>("type"))
                {
                    case "Computopic":
                        gateway = new ComputopicGateway(Configuration.GetSection(message.cvr).GetValue<string>("username"), Configuration.GetSection(message.cvr).GetValue<string>("password"));
                        break;
                    default:
                        log.Error("Unknown gateway type for " + message.cvr);

                        return BadRequest($"No Gateway mapping found for cvr: {message.cvr}");
                }
            }
            else
            {
                return BadRequest($"No Gateway mapping found for cvr: {message.cvr}");
            }

            if (message.numbers == null || message.numbers.Count == 0)
            {
                return BadRequest("No numbers supplied");
            }

            SanatizeNumbers(message);

            bool status = gateway.SendSMS(message);
            if (!status)
            {
                return BadRequest("Message not send!");
            }

            return "OK";
        }

        private void SanatizeNumbers(Message message)
        {
            List<string> numbers = new List<string>();

            foreach (string number in message.numbers)
            {
                string withoutWhitespace = number.Replace(" ", "");
                StringBuilder builder = new StringBuilder();

                foreach (char c in withoutWhitespace.ToCharArray())
                {
                    if (Char.IsDigit(c) || c == '+')
                    {
                        builder.Append(c);
                    }
                }

                string digitsOnly = builder.ToString();
                switch (digitsOnly.Length)
                {
                    case 8:
                    case 11:
                        numbers.Add(digitsOnly);
                        break;
                    case 10: // make sure to add the '+'
                        numbers.Add('+' + digitsOnly);
                        break;
                    default:
                        log.Warn("Discarding phonenumber: " + number);
                        break;
                }
            }

            message.numbers = numbers;
        }
    }
}
