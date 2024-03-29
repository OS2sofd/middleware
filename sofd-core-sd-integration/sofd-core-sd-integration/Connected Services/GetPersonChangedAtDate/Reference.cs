﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace GetPersonChangedAtDate
{
    
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ServiceModel.ServiceContractAttribute(Namespace="www.sd.dk/sdws/GetPersonChangedAtDate20111201", ConfigurationName="GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType")]
    public interface GetPersonChangedAtDate20111201PortType
    {
        
        // CODEGEN: Generating message contract since the operation GetPersonChangedAtDate20111201Operation is neither RPC nor document wrapped.
        [System.ServiceModel.OperationContractAttribute(Action="https://service.sd.dk/sdws/services/GetPersonChangedAtDate20111201", ReplyAction="*")]
        [System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)]
        [System.ServiceModel.ServiceKnownTypeAttribute(typeof(PersonContactInformationType))]
        GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse GetPersonChangedAtDate20111201Operation(GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest request);
        
        [System.ServiceModel.OperationContractAttribute(Action="https://service.sd.dk/sdws/services/GetPersonChangedAtDate20111201", ReplyAction="*")]
        System.Threading.Tasks.Task<GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse> GetPersonChangedAtDate20111201OperationAsync(GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest request);
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01")]
    public partial class GetPersonChangedAtDateRequestType
    {
        
        private string institutionIdentifierField;
        
        private string personCivilRegistrationIdentifierField;
        
        private string employmentIdentifierField;
        
        private string departmentIdentifierField;
        
        private string departmentLevelIdentifierField;
        
        private System.DateTime activationDateField;
        
        private System.DateTime activationTimeField;
        
        private bool activationTimeFieldSpecified;
        
        private System.DateTime deactivationDateField;
        
        private System.DateTime deactivationTimeField;
        
        private bool deactivationTimeFieldSpecified;
        
        private bool contactInformationIndicatorField;
        
        private bool postalAddressIndicatorField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public string InstitutionIdentifier
        {
            get
            {
                return this.institutionIdentifierField;
            }
            set
            {
                this.institutionIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/", Order=1)]
        public string PersonCivilRegistrationIdentifier
        {
            get
            {
                return this.personCivilRegistrationIdentifierField;
            }
            set
            {
                this.personCivilRegistrationIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=2)]
        public string EmploymentIdentifier
        {
            get
            {
                return this.employmentIdentifierField;
            }
            set
            {
                this.employmentIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=3)]
        public string DepartmentIdentifier
        {
            get
            {
                return this.departmentIdentifierField;
            }
            set
            {
                this.departmentIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/", Order=4)]
        public string DepartmentLevelIdentifier
        {
            get
            {
                return this.departmentLevelIdentifierField;
            }
            set
            {
                this.departmentLevelIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", DataType="date", Order=5)]
        public System.DateTime ActivationDate
        {
            get
            {
                return this.activationDateField;
            }
            set
            {
                this.activationDateField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", DataType="time", Order=6)]
        public System.DateTime ActivationTime
        {
            get
            {
                return this.activationTimeField;
            }
            set
            {
                this.activationTimeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool ActivationTimeSpecified
        {
            get
            {
                return this.activationTimeFieldSpecified;
            }
            set
            {
                this.activationTimeFieldSpecified = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", DataType="date", Order=7)]
        public System.DateTime DeactivationDate
        {
            get
            {
                return this.deactivationDateField;
            }
            set
            {
                this.deactivationDateField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", DataType="time", Order=8)]
        public System.DateTime DeactivationTime
        {
            get
            {
                return this.deactivationTimeField;
            }
            set
            {
                this.deactivationTimeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool DeactivationTimeSpecified
        {
            get
            {
                return this.deactivationTimeFieldSpecified;
            }
            set
            {
                this.deactivationTimeFieldSpecified = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", Order=9)]
        public bool ContactInformationIndicator
        {
            get
            {
                return this.contactInformationIndicatorField;
            }
            set
            {
                this.contactInformationIndicatorField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", Order=10)]
        public bool PostalAddressIndicator
        {
            get
            {
                return this.postalAddressIndicatorField;
            }
            set
            {
                this.postalAddressIndicatorField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01")]
    public partial class EmploymentType
    {
        
        private string employmentIdentifierField;
        
        private ContactInformationType contactInformationField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public string EmploymentIdentifier
        {
            get
            {
                return this.employmentIdentifierField;
            }
            set
            {
                this.employmentIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/", Order=1)]
        public ContactInformationType ContactInformation
        {
            get
            {
                return this.contactInformationField;
            }
            set
            {
                this.contactInformationField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/")]
    public partial class ContactInformationType
    {
        
        private string[] telephoneNumberIdentifierField;
        
        private string[] emailAddressIdentifierField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("TelephoneNumberIdentifier", Namespace="http://rep.oio.dk/itst.dk/xml/schemas/2005/01/10/", Order=0)]
        public string[] TelephoneNumberIdentifier
        {
            get
            {
                return this.telephoneNumberIdentifierField;
            }
            set
            {
                this.telephoneNumberIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("EmailAddressIdentifier", Namespace="http://rep.oio.dk/xkom.dk/xml/schemas/2005/03/15/", Order=1)]
        public string[] EmailAddressIdentifier
        {
            get
            {
                return this.emailAddressIdentifierField;
            }
            set
            {
                this.emailAddressIdentifierField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/")]
    public partial class CountryIdentificationCodeType
    {
        
        private _CountryIdentificationSchemeType schemeField;
        
        private string valueField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public _CountryIdentificationSchemeType scheme
        {
            get
            {
                return this.schemeField;
            }
            set
            {
                this.schemeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value
        {
            get
            {
                return this.valueField;
            }
            set
            {
                this.valueField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/")]
    public enum _CountryIdentificationSchemeType
    {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("iso3166-alpha2")]
        iso3166alpha2,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("iso3166-alpha3")]
        iso3166alpha3,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("un-numeric3")]
        unnumeric3,
        
        /// <remarks/>
        imk,
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/")]
    public partial class PostalAddressType
    {
        
        private string standardAddressIdentifierField;
        
        private string postalCodeField;
        
        private string districtNameField;
        
        private string municipalityCodeField;
        
        private CountryIdentificationCodeType countryIdentificationCodeField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/itst.dk/xml/schemas/2005/06/24/", Order=0)]
        public string StandardAddressIdentifier
        {
            get
            {
                return this.standardAddressIdentifierField;
            }
            set
            {
                this.standardAddressIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=1)]
        public string PostalCode
        {
            get
            {
                return this.postalCodeField;
            }
            set
            {
                this.postalCodeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/03/15/", Order=2)]
        public string DistrictName
        {
            get
            {
                return this.districtNameField;
            }
            set
            {
                this.districtNameField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/", Order=3)]
        public string MunicipalityCode
        {
            get
            {
                return this.municipalityCodeField;
            }
            set
            {
                this.municipalityCodeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/", Order=4)]
        public CountryIdentificationCodeType CountryIdentificationCode
        {
            get
            {
                return this.countryIdentificationCodeField;
            }
            set
            {
                this.countryIdentificationCodeField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(PersonType))]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01")]
    public partial class PersonContactInformationType
    {
        
        private string personCivilRegistrationIdentifierField;
        
        private string personGivenNameField;
        
        private string personSurnameNameField;
        
        private PostalAddressType postalAddressField;
        
        private ContactInformationType contactInformationField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/", Order=0)]
        public string PersonCivilRegistrationIdentifier
        {
            get
            {
                return this.personCivilRegistrationIdentifierField;
            }
            set
            {
                this.personCivilRegistrationIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/", Order=1)]
        public string PersonGivenName
        {
            get
            {
                return this.personGivenNameField;
            }
            set
            {
                this.personGivenNameField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/", Order=2)]
        public string PersonSurnameName
        {
            get
            {
                return this.personSurnameNameField;
            }
            set
            {
                this.personSurnameNameField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", Order=3)]
        public PostalAddressType PostalAddress
        {
            get
            {
                return this.postalAddressField;
            }
            set
            {
                this.postalAddressField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/", Order=4)]
        public ContactInformationType ContactInformation
        {
            get
            {
                return this.contactInformationField;
            }
            set
            {
                this.contactInformationField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01")]
    public partial class PersonType : PersonContactInformationType
    {
        
        private EmploymentType[] employmentField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Employment", Order=0)]
        public EmploymentType[] Employment
        {
            get
            {
                return this.employmentField;
            }
            set
            {
                this.employmentField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01")]
    public partial class GetPersonType
    {
        
        private GetPersonChangedAtDateRequestType requestStructureField;
        
        private PersonType[] personField;
        
        private System.DateTime creationDateTimeField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public GetPersonChangedAtDateRequestType RequestStructure
        {
            get
            {
                return this.requestStructureField;
            }
            set
            {
                this.requestStructureField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Person", Order=1)]
        public PersonType[] Person
        {
            get
            {
                return this.personField;
            }
            set
            {
                this.personField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public System.DateTime creationDateTime
        {
            get
            {
                return this.creationDateTimeField;
            }
            set
            {
                this.creationDateTimeField = value;
            }
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class GetPersonChangedAtDate20111201OperationRequest
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01", Order=0)]
        public GetPersonChangedAtDate.GetPersonChangedAtDateRequestType GetPersonChangedAtDate;
        
        public GetPersonChangedAtDate20111201OperationRequest()
        {
        }
        
        public GetPersonChangedAtDate20111201OperationRequest(GetPersonChangedAtDate.GetPersonChangedAtDateRequestType GetPersonChangedAtDate)
        {
            this.GetPersonChangedAtDate = GetPersonChangedAtDate;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class GetPersonChangedAtDate20111201OperationResponse
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01", Order=0)]
        public GetPersonChangedAtDate.GetPersonType GetPersonChangedAtDate20111201;
        
        public GetPersonChangedAtDate20111201OperationResponse()
        {
        }
        
        public GetPersonChangedAtDate20111201OperationResponse(GetPersonChangedAtDate.GetPersonType GetPersonChangedAtDate20111201)
        {
            this.GetPersonChangedAtDate20111201 = GetPersonChangedAtDate20111201;
        }
    }
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public interface GetPersonChangedAtDate20111201PortTypeChannel : GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType, System.ServiceModel.IClientChannel
    {
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public partial class GetPersonChangedAtDate20111201PortTypeClient : System.ServiceModel.ClientBase<GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType>, GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType
    {
        
        /// <summary>
        /// Implement this partial method to configure the service endpoint.
        /// </summary>
        /// <param name="serviceEndpoint">The endpoint to configure</param>
        /// <param name="clientCredentials">The client credentials</param>
        static partial void ConfigureEndpoint(System.ServiceModel.Description.ServiceEndpoint serviceEndpoint, System.ServiceModel.Description.ClientCredentials clientCredentials);
        
        public GetPersonChangedAtDate20111201PortTypeClient() : 
                base(GetPersonChangedAtDate20111201PortTypeClient.GetDefaultBinding(), GetPersonChangedAtDate20111201PortTypeClient.GetDefaultEndpointAddress())
        {
            this.Endpoint.Name = EndpointConfiguration.GetPersonChangedAtDate20111201.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetPersonChangedAtDate20111201PortTypeClient(EndpointConfiguration endpointConfiguration) : 
                base(GetPersonChangedAtDate20111201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), GetPersonChangedAtDate20111201PortTypeClient.GetEndpointAddress(endpointConfiguration))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetPersonChangedAtDate20111201PortTypeClient(EndpointConfiguration endpointConfiguration, string remoteAddress) : 
                base(GetPersonChangedAtDate20111201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), new System.ServiceModel.EndpointAddress(remoteAddress))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetPersonChangedAtDate20111201PortTypeClient(EndpointConfiguration endpointConfiguration, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(GetPersonChangedAtDate20111201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), remoteAddress)
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetPersonChangedAtDate20111201PortTypeClient(System.ServiceModel.Channels.Binding binding, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(binding, remoteAddress)
        {
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType.GetPersonChangedAtDate20111201Operation(GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest request)
        {
            return base.Channel.GetPersonChangedAtDate20111201Operation(request);
        }
        
        public GetPersonChangedAtDate.GetPersonType GetPersonChangedAtDate20111201Operation(GetPersonChangedAtDate.GetPersonChangedAtDateRequestType GetPersonChangedAtDate)
        {
            GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest inValue = new GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest();
            inValue.GetPersonChangedAtDate = GetPersonChangedAtDate;
            GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse retVal = ((GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType)(this)).GetPersonChangedAtDate20111201Operation(inValue);
            return retVal.GetPersonChangedAtDate20111201;
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        System.Threading.Tasks.Task<GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse> GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType.GetPersonChangedAtDate20111201OperationAsync(GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest request)
        {
            return base.Channel.GetPersonChangedAtDate20111201OperationAsync(request);
        }
        
        public System.Threading.Tasks.Task<GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationResponse> GetPersonChangedAtDate20111201OperationAsync(GetPersonChangedAtDate.GetPersonChangedAtDateRequestType GetPersonChangedAtDate)
        {
            GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest inValue = new GetPersonChangedAtDate.GetPersonChangedAtDate20111201OperationRequest();
            inValue.GetPersonChangedAtDate = GetPersonChangedAtDate;
            return ((GetPersonChangedAtDate.GetPersonChangedAtDate20111201PortType)(this)).GetPersonChangedAtDate20111201OperationAsync(inValue);
        }
        
        public virtual System.Threading.Tasks.Task OpenAsync()
        {
            return System.Threading.Tasks.Task.Factory.FromAsync(((System.ServiceModel.ICommunicationObject)(this)).BeginOpen(null, null), new System.Action<System.IAsyncResult>(((System.ServiceModel.ICommunicationObject)(this)).EndOpen));
        }
        
        public virtual System.Threading.Tasks.Task CloseAsync()
        {
            return System.Threading.Tasks.Task.Factory.FromAsync(((System.ServiceModel.ICommunicationObject)(this)).BeginClose(null, null), new System.Action<System.IAsyncResult>(((System.ServiceModel.ICommunicationObject)(this)).EndClose));
        }
        
        private static System.ServiceModel.Channels.Binding GetBindingForEndpoint(EndpointConfiguration endpointConfiguration)
        {
            if ((endpointConfiguration == EndpointConfiguration.GetPersonChangedAtDate20111201))
            {
                System.ServiceModel.BasicHttpBinding result = new System.ServiceModel.BasicHttpBinding();
                result.MaxBufferSize = int.MaxValue;
                result.ReaderQuotas = System.Xml.XmlDictionaryReaderQuotas.Max;
                result.MaxReceivedMessageSize = int.MaxValue;
                result.AllowCookies = true;
                result.Security.Mode = System.ServiceModel.BasicHttpSecurityMode.Transport;
                return result;
            }
            throw new System.InvalidOperationException(string.Format("Could not find endpoint with name \'{0}\'.", endpointConfiguration));
        }
        
        private static System.ServiceModel.EndpointAddress GetEndpointAddress(EndpointConfiguration endpointConfiguration)
        {
            if ((endpointConfiguration == EndpointConfiguration.GetPersonChangedAtDate20111201))
            {
                return new System.ServiceModel.EndpointAddress("https://service.sd.dk/sdws/services/GetPersonChangedAtDate20111201");
            }
            throw new System.InvalidOperationException(string.Format("Could not find endpoint with name \'{0}\'.", endpointConfiguration));
        }
        
        private static System.ServiceModel.Channels.Binding GetDefaultBinding()
        {
            return GetPersonChangedAtDate20111201PortTypeClient.GetBindingForEndpoint(EndpointConfiguration.GetPersonChangedAtDate20111201);
        }
        
        private static System.ServiceModel.EndpointAddress GetDefaultEndpointAddress()
        {
            return GetPersonChangedAtDate20111201PortTypeClient.GetEndpointAddress(EndpointConfiguration.GetPersonChangedAtDate20111201);
        }
        
        public enum EndpointConfiguration
        {
            
            GetPersonChangedAtDate20111201,
        }
    }
}
