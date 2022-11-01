using System;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using SofdCprIntegration;

namespace PersonBaseDataExtendedService
{
    public partial class PersonBaseDataExtendedPortTypeClient : System.ServiceModel.ClientBase<PersonBaseDataExtendedService.PersonBaseDataExtendedPortType>, PersonBaseDataExtendedService.PersonBaseDataExtendedPortType
    {
        public PersonBaseDataExtendedPortTypeClient(string endpointUrl, string certPath, string certPassword, bool enableTraceLogging)
        : base(PersonBaseDataExtendedPortTypeClient.GetBindingForEndpoint(), PersonBaseDataExtendedPortTypeClient.GetEndpointAddress(endpointUrl))
        {
            this.ClientCredentials.ClientCertificate.Certificate = new X509Certificate2(fileName: certPath, password: SecureStringUtil.ConvertToSecureString(certPassword));

            // Disable revocation checking
            this.ClientCredentials.ServiceCertificate.Authentication.RevocationMode = X509RevocationMode.NoCheck;

            // enable trace logging
            if (enableTraceLogging)
            {
                this.ChannelFactory.Endpoint.EndpointBehaviors.Add(new LoggingBehavior());
            }

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
