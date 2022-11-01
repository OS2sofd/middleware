﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace GetProfession
{
    
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ServiceModel.ServiceContractAttribute(Namespace="www.sd.dk/sdws/GetProfession20080201", ConfigurationName="GetProfession.GetProfession20080201PortType")]
    public interface GetProfession20080201PortType
    {
        
        // CODEGEN: Generating message contract since the operation GetProfession20080201Operation is neither RPC nor document wrapped.
        [System.ServiceModel.OperationContractAttribute(Action="", ReplyAction="*")]
        [System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)]
        GetProfession.GetProfession20080201OperationResponse GetProfession20080201Operation(GetProfession.GetProfession20080201OperationRequest request);
        
        [System.ServiceModel.OperationContractAttribute(Action="", ReplyAction="*")]
        System.Threading.Tasks.Task<GetProfession.GetProfession20080201OperationResponse> GetProfession20080201OperationAsync(GetProfession.GetProfession20080201OperationRequest request);
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/GetProfession20080201")]
    public partial class GetProfessionRequestType
    {
        
        private string institutionIdentifierField;
        
        private string jobPositionIdentifierField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", Order=0)]
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
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070401/", Order=1)]
        public string JobPositionIdentifier
        {
            get
            {
                return this.jobPositionIdentifierField;
            }
            set
            {
                this.jobPositionIdentifierField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/")]
    public partial class ProfessionType
    {
        
        private string jobPositionIdentifierField;
        
        private string jobPositionNameField;
        
        private JobPositionLevelCodeType jobPositionLevelCodeField;
        
        private ProfessionType[] professionField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public string JobPositionIdentifier
        {
            get
            {
                return this.jobPositionIdentifierField;
            }
            set
            {
                this.jobPositionIdentifierField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=1)]
        public string JobPositionName
        {
            get
            {
                return this.jobPositionNameField;
            }
            set
            {
                this.jobPositionNameField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=2)]
        public JobPositionLevelCodeType JobPositionLevelCode
        {
            get
            {
                return this.jobPositionLevelCodeField;
            }
            set
            {
                this.jobPositionLevelCodeField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Profession", Order=3)]
        public ProfessionType[] Profession
        {
            get
            {
                return this.professionField;
            }
            set
            {
                this.professionField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/")]
    public enum JobPositionLevelCodeType
    {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("0")]
        Item0,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("1")]
        Item1,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("2")]
        Item2,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("3")]
        Item3,
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/")]
    public partial class GetProfessionType
    {
        
        private GetProfessionRequestType requestKeyField;
        
        private ProfessionType[] professionField;
        
        private System.DateTime creationTimeField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public GetProfessionRequestType RequestKey
        {
            get
            {
                return this.requestKeyField;
            }
            set
            {
                this.requestKeyField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Profession", Order=1)]
        public ProfessionType[] Profession
        {
            get
            {
                return this.professionField;
            }
            set
            {
                this.professionField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public System.DateTime creationTime
        {
            get
            {
                return this.creationTimeField;
            }
            set
            {
                this.creationTimeField = value;
            }
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class GetProfession20080201OperationRequest
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/GetProfession20080201", Order=0)]
        public GetProfession.GetProfessionRequestType GetProfession20080201;
        
        public GetProfession20080201OperationRequest()
        {
        }
        
        public GetProfession20080201OperationRequest(GetProfession.GetProfessionRequestType GetProfession20080201)
        {
            this.GetProfession20080201 = GetProfession20080201;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class GetProfession20080201OperationResponse
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20080201/", Order=0)]
        public GetProfession.GetProfessionType GetProfession20080201;
        
        public GetProfession20080201OperationResponse()
        {
        }
        
        public GetProfession20080201OperationResponse(GetProfession.GetProfessionType GetProfession20080201)
        {
            this.GetProfession20080201 = GetProfession20080201;
        }
    }
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public interface GetProfession20080201PortTypeChannel : GetProfession.GetProfession20080201PortType, System.ServiceModel.IClientChannel
    {
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public partial class GetProfession20080201PortTypeClient : System.ServiceModel.ClientBase<GetProfession.GetProfession20080201PortType>, GetProfession.GetProfession20080201PortType
    {
        
        /// <summary>
        /// Implement this partial method to configure the service endpoint.
        /// </summary>
        /// <param name="serviceEndpoint">The endpoint to configure</param>
        /// <param name="clientCredentials">The client credentials</param>
        static partial void ConfigureEndpoint(System.ServiceModel.Description.ServiceEndpoint serviceEndpoint, System.ServiceModel.Description.ClientCredentials clientCredentials);
        
        public GetProfession20080201PortTypeClient() : 
                base(GetProfession20080201PortTypeClient.GetDefaultBinding(), GetProfession20080201PortTypeClient.GetDefaultEndpointAddress())
        {
            this.Endpoint.Name = EndpointConfiguration.GetProfession20080201.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetProfession20080201PortTypeClient(EndpointConfiguration endpointConfiguration) : 
                base(GetProfession20080201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), GetProfession20080201PortTypeClient.GetEndpointAddress(endpointConfiguration))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetProfession20080201PortTypeClient(EndpointConfiguration endpointConfiguration, string remoteAddress) : 
                base(GetProfession20080201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), new System.ServiceModel.EndpointAddress(remoteAddress))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetProfession20080201PortTypeClient(EndpointConfiguration endpointConfiguration, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(GetProfession20080201PortTypeClient.GetBindingForEndpoint(endpointConfiguration), remoteAddress)
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetProfession20080201PortTypeClient(System.ServiceModel.Channels.Binding binding, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(binding, remoteAddress)
        {
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        GetProfession.GetProfession20080201OperationResponse GetProfession.GetProfession20080201PortType.GetProfession20080201Operation(GetProfession.GetProfession20080201OperationRequest request)
        {
            return base.Channel.GetProfession20080201Operation(request);
        }
        
        public GetProfession.GetProfessionType GetProfession20080201Operation(GetProfession.GetProfessionRequestType GetProfession20080201)
        {
            GetProfession.GetProfession20080201OperationRequest inValue = new GetProfession.GetProfession20080201OperationRequest();
            inValue.GetProfession20080201 = GetProfession20080201;
            GetProfession.GetProfession20080201OperationResponse retVal = ((GetProfession.GetProfession20080201PortType)(this)).GetProfession20080201Operation(inValue);
            return retVal.GetProfession20080201;
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        System.Threading.Tasks.Task<GetProfession.GetProfession20080201OperationResponse> GetProfession.GetProfession20080201PortType.GetProfession20080201OperationAsync(GetProfession.GetProfession20080201OperationRequest request)
        {
            return base.Channel.GetProfession20080201OperationAsync(request);
        }
        
        public System.Threading.Tasks.Task<GetProfession.GetProfession20080201OperationResponse> GetProfession20080201OperationAsync(GetProfession.GetProfessionRequestType GetProfession20080201)
        {
            GetProfession.GetProfession20080201OperationRequest inValue = new GetProfession.GetProfession20080201OperationRequest();
            inValue.GetProfession20080201 = GetProfession20080201;
            return ((GetProfession.GetProfession20080201PortType)(this)).GetProfession20080201OperationAsync(inValue);
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
            if ((endpointConfiguration == EndpointConfiguration.GetProfession20080201))
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
            if ((endpointConfiguration == EndpointConfiguration.GetProfession20080201))
            {
                return new System.ServiceModel.EndpointAddress("https://service.sd.dk/sdws/services/GetProfession20080201");
            }
            throw new System.InvalidOperationException(string.Format("Could not find endpoint with name \'{0}\'.", endpointConfiguration));
        }
        
        private static System.ServiceModel.Channels.Binding GetDefaultBinding()
        {
            return GetProfession20080201PortTypeClient.GetBindingForEndpoint(EndpointConfiguration.GetProfession20080201);
        }
        
        private static System.ServiceModel.EndpointAddress GetDefaultEndpointAddress()
        {
            return GetProfession20080201PortTypeClient.GetEndpointAddress(EndpointConfiguration.GetProfession20080201);
        }
        
        public enum EndpointConfiguration
        {
            
            GetProfession20080201,
        }
    }
}
