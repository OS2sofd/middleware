using Microsoft.AspNet.OData.Query;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Extensions.DependencyInjection;
using Newtonsoft.Json;
using SOFDCore.ODataApi.Database;
using SOFDCore.ODataApi.Models;
using System;
using System.Net;
using System.Linq;
using System.Security.Claims;

namespace SOFDCore.ODataApi.Filters
{
    public class AuditlogFilter : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext context)
        {
            var auditContext = context.HttpContext.RequestServices.GetRequiredService<AuditContext>();

            int idOnRequest = int.Parse(context.HttpContext.User.Claims.Where(c => c.Type == ClaimTypes.NameIdentifier).First().Value);

            var securityLog = new SecurityLog();
            securityLog.ClientId = idOnRequest;
            securityLog.Clientname = context.HttpContext.User.Identity.Name;
            securityLog.IpAddress = context.HttpContext.Request.Headers["x-forwarded-for"].ToString() ?? "";
            securityLog.Method = context.HttpContext.Request.Method;
            securityLog.Request = $"uri={context.HttpContext.Request.Path}{WebUtility.UrlDecode(context.HttpContext.Request.QueryString.Value)}";
            securityLog.timestamp = DateTime.Now;
            auditContext.Add(securityLog);
            auditContext.SaveChanges();

            // TLS check
            string tlsVersionOnRequest = context.HttpContext.Request.Headers["x-amzn-tls-version"].ToString() ?? "";
            string tlsVersionOnClient = context.HttpContext.User.Claims.Where(c => c.Type.Equals("tlsVersion")).First().Value ?? "";

            if (!string.IsNullOrEmpty(tlsVersionOnRequest) && !tlsVersionOnRequest.Equals(tlsVersionOnClient))
            {
                var client = auditContext.Clients.Where(c => c.Id == idOnRequest).FirstOrDefault();
                if (client != null)
                {
                    client.TlsVersion = tlsVersionOnRequest;
                    auditContext.SaveChanges();
                }
            }

            base.OnActionExecuting(context);
        }

        private string GetODataParameters(ActionExecutingContext context)
        {
            try
            {
                return JsonConvert.SerializeObject(((ODataQueryOptions)context.ActionArguments["queryOptions"]).RawValues);
            }
            catch (Exception)
            {
                // No paramater of type ODataQueryOptions was found in actionContext.ActionArguments, which is fine.
                return null;
            }
        }
    }
}
