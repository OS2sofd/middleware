using SofdCprIntegration;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.Threading.Tasks;

namespace CprSubscriptionService
{
    public partial class CprSubscriptionWebServicePortTypeClient : System.ServiceModel.ClientBase<CprSubscriptionService.CprSubscriptionWebServicePortType>, CprSubscriptionService.CprSubscriptionWebServicePortType
    {
        public CprSubscriptionWebServicePortTypeClient(string endpointUrl, string certPath, string certPassword)
            : base(CprSubscriptionWebServicePortTypeClient.GetBindingForEndpoint(), CprSubscriptionWebServicePortTypeClient.GetEndpointAddress(endpointUrl))
        {
            this.ClientCredentials.ClientCertificate.Certificate = new X509Certificate2(fileName: certPath, password: SecureStringUtil.ConvertToSecureString(certPassword));

            // Disable revocation checking
            this.ClientCredentials.ServiceCertificate.Authentication.RevocationMode = X509RevocationMode.NoCheck;

            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }

        private static System.ServiceModel.Channels.Binding GetBindingForEndpoint()
        {
            BasicHttpBinding binding = new BasicHttpBinding();
            binding.Security.Mode = BasicHttpSecurityMode.Transport;
            binding.Security.Transport.ClientCredentialType = HttpClientCredentialType.Certificate;
            binding.MaxReceivedMessageSize = Int32.MaxValue;
            binding.OpenTimeout = new TimeSpan(0, 0, 30);
            binding.CloseTimeout = new TimeSpan(0, 0, 30);
            binding.ReceiveTimeout = new TimeSpan(0, 0, 30);
            binding.ReceiveTimeout = new TimeSpan(0, 0, 30);
            binding.SendTimeout = new TimeSpan(0, 0, 30);

            return binding;
        }

        private static System.ServiceModel.EndpointAddress GetEndpointAddress(string endpointUrl)
        {
            return new System.ServiceModel.EndpointAddress(endpointUrl);
        }
    }
}
