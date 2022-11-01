using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;

namespace PersonBaseDataExtendedService
{
    internal class LoggingBehavior : IEndpointBehavior
    {

        public LoggingBehavior()
        {
        }

        public void Validate(ServiceEndpoint endpoint)
        {
        }

        public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
        }

        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
        {
        }

        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(new LoggingMessageInspector());
        }
    }
}
