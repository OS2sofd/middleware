using CprSubscriptionService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;

namespace SofdCprIntegration
{
    [ApiController]
    [Produces("application/json")]
    [Route("api/init")]
    public class SettingController : ControllerBase
    {
        private IConfiguration Configuration;

        public SettingController(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        [HttpGet]
        public ActionResult Init()
        {
            //Call the "subscription" webservice to add this CPR number to the list of CPR numbers that we want to subscribe to changes to
            CprSubscriptionWebServicePortTypeClient subscriptionService = new CprSubscriptionWebServicePortTypeClient(Configuration["SubscriptionService:serviceUrl"], Configuration["SubscriptionService:certPath"], Configuration["SubscriptionService:certPassword"]);

            RemoveAllType removeType = new RemoveAllType();
            removeType.FilterGroup = FilterGroup.EventChangeCode;
            var tmp = subscriptionService.RemoveAllAsync(removeType).Result; // block call, so we are sure everything is gone before adding subscriptions

            addChangeCodeSubscription(subscriptionService, "A01");
            addChangeCodeSubscription(subscriptionService, "A04");
            addChangeCodeSubscription(subscriptionService, "A07");
            addChangeCodeSubscription(subscriptionService, "A09");
            addChangeCodeSubscription(subscriptionService, "A13");
            addChangeCodeSubscription(subscriptionService, "A14");
            addChangeCodeSubscription(subscriptionService, "A15");
            addChangeCodeSubscription(subscriptionService, "A16");
            addChangeCodeSubscription(subscriptionService, "A17");
            addChangeCodeSubscription(subscriptionService, "A33");
            addChangeCodeSubscription(subscriptionService, "A42");
            addChangeCodeSubscription(subscriptionService, "P10");
            addChangeCodeSubscription(subscriptionService, "P11");

/* can be used to test that the above is completed - sleep a bit before calling though

            GetAllFiltersType getAllFilters = new GetAllFiltersType();
            getAllFilters.InvocationContext = GetInvocationContext();
            GetAllFiltersResponse allFilters = subscriptionService.GetAllFiltersAsync(getAllFilters).Result;
*/
            // A01, A04, A07, A09, A13, A14, A15, A16, A17, A33, A42, P10, P11

            return Ok();
        }

        private void addChangeCodeSubscription(CprSubscriptionWebServicePortTypeClient subscriptionService, string code)
        {
            AddChangeCodeSubscriptionType subscription = new AddChangeCodeSubscriptionType();
            subscription.InvocationContext = GetInvocationContext();
            subscription.ChangeCode = code;
            subscriptionService.AddChangeCodeSubscriptionAsync(subscription);
        }

        private InvocationContextType GetInvocationContext()
        {
            InvocationContextType invocationContext = new CprSubscriptionService.InvocationContextType();

            // these values match a specific service-agreement with Næstved. If that ever changes, change these values (or extract to configuration)
            invocationContext.ServiceUUID = "9cdccc2f-3243-11e2-8fef-d4bed98c5934";
            invocationContext.ServiceAgreementUUID = "579e05e9-1bf3-4670-a2b6-9c99063e5f1a";
            invocationContext.UserUUID = "1f3e6b87-1ae8-470a-9cba-5d996c135eca";
            invocationContext.UserSystemUUID = "ce062d24-31c0-4abf-92af-66f42c863296";

            return invocationContext;
        }
    }
}