using System;
using System.ServiceModel;

namespace DigitalIdentity.SD
{
    class SDServiceStubs : BaseClass<SDServiceStubs>
    {
        public GetPerson.GetPerson20111201PortTypeClient GetPersonClient { get; }
        public GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortTypeClient GetPersonChangedAtDateClient { get; }
        public GetEmployment.GetEmployment20111201PortTypeClient GetEmploymentClient { get; }
        public GetEmploymentChanged.GetEmploymentChanged20111201PortTypeClient GetEmploymentChangedClient { get;}
        public GetEmploymentChangedAtDate.GetEmploymentChangedAtDate20111201PortTypeClient GetEmploymentChangedAtDateClient { get;}
        public GetDepartment.GetDepartment20111201PortTypeClient GetDepartmentClient { get;}
        public GetDepartmentParent.GetDepartmentParent20190701PortTypeClient GetDepartmentParentClient { get; }
        public GetInstitution.GetInstitution20111201PortTypeClient GetInstitutionClient { get; }
        public GetOrganization.GetOrganization20111201PortTypeClient GetOrganizationClient { get;}
        public GetProfession.GetProfession20080201PortTypeClient GetProfessionClient { get;}

        public SDServiceStubs(IServiceProvider sp) : base(sp) { 
            GetPersonClient = GetServiceClient<GetPerson.GetPerson20111201PortTypeClient, GetPerson.GetPerson20111201PortType>("GetPerson20111201");
            GetPersonChangedAtDateClient = GetServiceClient<GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortTypeClient, GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType>("GetPersonChangedAtDate20111201");
            GetEmploymentClient = GetServiceClient<GetEmployment.GetEmployment20111201PortTypeClient, GetEmployment.GetEmployment20111201PortType>("GetEmployment20111201");
            GetEmploymentChangedClient = GetServiceClient<GetEmploymentChanged.GetEmploymentChanged20111201PortTypeClient, GetEmploymentChanged.GetEmploymentChanged20111201PortType>("GetEmploymentChanged20111201");
            GetEmploymentChangedAtDateClient = GetServiceClient<GetEmploymentChangedAtDate.GetEmploymentChangedAtDate20111201PortTypeClient, GetEmploymentChangedAtDate.GetEmploymentChangedAtDate20111201PortType>("GetEmploymentChangedAtDate20111201");
            GetDepartmentClient = GetServiceClient<GetDepartment.GetDepartment20111201PortTypeClient, GetDepartment.GetDepartment20111201PortType>("GetDepartment20111201");
            GetDepartmentParentClient = GetServiceClient<GetDepartmentParent.GetDepartmentParent20190701PortTypeClient, GetDepartmentParent.GetDepartmentParent20190701PortType>("GetDepartmentParent20190701");
            GetInstitutionClient = GetServiceClient<GetInstitution.GetInstitution20111201PortTypeClient, GetInstitution.GetInstitution20111201PortType>("GetInstitution20111201");
            GetOrganizationClient = GetServiceClient<GetOrganization.GetOrganization20111201PortTypeClient, GetOrganization.GetOrganization20111201PortType>("GetOrganization20111201");
            GetProfessionClient = GetServiceClient<GetProfession.GetProfession20080201PortTypeClient, GetProfession.GetProfession20080201PortType>("GetProfession20080201");
        }

        private  TClient GetServiceClient<TClient, TInterface>(string endpoint) 
            where TClient : class 
            where TInterface : class
        {
            var result = Activator.CreateInstance<TClient>() as TClient;
            var client = result as ClientBase<TInterface>;
            client.Endpoint.Address = new EndpointAddress(appSettings.SDSettings.BaseUrl + endpoint);
            var binding = new BasicHttpsBinding();
            binding.Security.Mode = BasicHttpsSecurityMode.Transport;
            binding.Security.Transport.ClientCredentialType = HttpClientCredentialType.Basic;
            binding.MaxReceivedMessageSize = Int32.MaxValue;
            binding.OpenTimeout = new TimeSpan(0, 3, 0);
            binding.CloseTimeout = new TimeSpan(0, 3, 0);
            binding.ReceiveTimeout = new TimeSpan(0, 3, 0);
            binding.SendTimeout = new TimeSpan(0, 3, 0);
            client.Endpoint.Binding = binding;
            client.ClientCredentials.UserName.UserName = appSettings.SDSettings.Username;
            client.ClientCredentials.UserName.Password = appSettings.SDSettings.Password;
            return result;
        }
    }
}