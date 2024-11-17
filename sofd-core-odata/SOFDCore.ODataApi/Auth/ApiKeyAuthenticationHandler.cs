using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using SOFDCore.ODataApi.Database;
using SOFDCore.ODataApi.Models;
using System.Collections.Generic;
using System;
using System.Linq;
using System.Net;
using System.Security.Claims;
using System.Text.Encodings.Web;
using System.Threading.Tasks;

namespace SOFDCore.ODataApi.Auth
{
    public class ApiKeyAuthenticationHandler : AuthenticationHandler<ApiKeyAuthenticationOptions>
    {
        private const string ProblemDetailsContentType = "application/problem+json";
        private readonly SOFDContext sofdContext;
        private const string ApiKeyHeaderName = "ApiKey";
        public ApiKeyAuthenticationHandler(
            IOptionsMonitor<ApiKeyAuthenticationOptions> options,
            ILoggerFactory logger,
            UrlEncoder encoder,
            ISystemClock clock,
            SOFDContext sofdContext) : base(options, logger, encoder, clock)
        {
            this.sofdContext = sofdContext;
        }

        protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
        {
            if (!Request.Headers.TryGetValue(ApiKeyHeaderName, out var apiKeyHeaderValues))
            {
                return AuthenticateResult.NoResult();
            }

            var providedApiKey = apiKeyHeaderValues.FirstOrDefault<string>();

            if (apiKeyHeaderValues.Count == 0 || string.IsNullOrWhiteSpace(providedApiKey))
            {
                return AuthenticateResult.NoResult();
            }

            var client = await sofdContext.Clients.Where(c => c.ApiKey == providedApiKey).Include(c => c.AccessFields).FirstOrDefaultAsync();

            if (client != null)
            {
                var claims = new List<Claim>{
                    new Claim(ClaimTypes.Role, client.AccessRole),
                    new Claim(ClaimTypes.Name, client.Name),
                    new Claim("tlsVersion", client.TlsVersion ?? ""),
                    new Claim(ClaimTypes.NameIdentifier, client.Id.ToString())
                };
                foreach (var accessField in client.AccessFields)
                {
                    claims.Add(new Claim(ClaimTypes.Role, accessField.Field));
                }
                var identity = new ClaimsIdentity(claims, Options.AuthenticationType);
                var identities = new List<ClaimsIdentity> { identity };
                var principal = new ClaimsPrincipal(identities);
                var ticket = new AuthenticationTicket(principal, Options.Scheme);

                List<IpRange> whitelist = new List<IpRange>();
                var allowedRanges = sofdContext.IpRanges.Where(ip => ip.ClientId == client.Id).ToList();
                if(allowedRanges != null)
                {
                    whitelist = allowedRanges;
                }
                if(IpWhitelistMatcher(whitelist, Context))
                {
                    return AuthenticateResult.Success(ticket);
                }
                else
                {
                    return AuthenticateResult.Fail("IP not whitelisted for provided API Key.");
                }
            }

            return AuthenticateResult.Fail("Invalid API Key provided.");
        }

        protected override async Task HandleChallengeAsync(AuthenticationProperties properties)
        {
            Response.StatusCode = 401;
            Response.ContentType = ProblemDetailsContentType;
            var problemDetails = new UnauthorizedProblemDetails();

            await Response.WriteAsync(JsonConvert.SerializeObject(problemDetails));
        }

        protected override async Task HandleForbiddenAsync(AuthenticationProperties properties)
        {
            Response.StatusCode = 403;
            Response.ContentType = ProblemDetailsContentType;
            var problemDetails = new ForbiddenProblemDetails();

            await Response.WriteAsync(JsonConvert.SerializeObject(problemDetails));
        }

        public bool IpWhitelistMatcher(List<IpRange> whitelist, HttpContext context)
        {
            if(whitelist == null || whitelist.Count==0)
            {
                return true;
            }
            var ip = IPAddress.Parse(context.Connection.RemoteIpAddress.ToString());
            foreach(IpRange ipRange in whitelist)
            {
                IPNetwork2 network = IPNetwork2.Parse(ipRange.Ip);
                if(network.Contains(ip))
                {
                    return true;
                }
            }
            Logger.LogWarning($"Did not find valid network range for IP: {ip}");
            return false;
        }
    }
}
