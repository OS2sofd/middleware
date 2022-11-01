﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace GetDepartmentParent
{
    
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ServiceModel.ServiceContractAttribute(Namespace="www.sd.dk/sdws/GetDepartmentParent20190701", ConfigurationName="GetDepartmentParent.GetDepartmentParent20190701PortType")]
    public interface GetDepartmentParent20190701PortType
    {
        
        // CODEGEN: Generating message contract since the operation GetDepartmentParent20190701Operation is neither RPC nor document wrapped.
        [System.ServiceModel.OperationContractAttribute(Action="https://service.sd.dk/sdws/services/GetDepartmentParent20190701", ReplyAction="*")]
        [System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)]
        GetDepartmentParent.GetDepartmentParent20190701OperationResponse GetDepartmentParent20190701Operation(GetDepartmentParent.GetDepartmentParent20190701OperationRequest request);
        
        [System.ServiceModel.OperationContractAttribute(Action="https://service.sd.dk/sdws/services/GetDepartmentParent20190701", ReplyAction="*")]
        System.Threading.Tasks.Task<GetDepartmentParent.GetDepartmentParent20190701OperationResponse> GetDepartmentParent20190701OperationAsync(GetDepartmentParent.GetDepartmentParent20190701OperationRequest request);
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2019.07.01")]
    public partial class GetDepartmentParentRequestType
    {
        
        private System.DateTime effectiveDateField;
        
        private bool effectiveDateFieldSpecified;
        
        private string departmentUUIDIdentifierField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="http://rep.oio.dk/sd.dk/xml.schema/20070301/", DataType="date", Order=0)]
        public System.DateTime EffectiveDate
        {
            get
            {
                return this.effectiveDateField;
            }
            set
            {
                this.effectiveDateField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool EffectiveDateSpecified
        {
            get
            {
                return this.effectiveDateFieldSpecified;
            }
            set
            {
                this.effectiveDateFieldSpecified = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01", Order=1)]
        public string DepartmentUUIDIdentifier
        {
            get
            {
                return this.departmentUUIDIdentifierField;
            }
            set
            {
                this.departmentUUIDIdentifierField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2019.07.01")]
    public partial class DepartmentParentType
    {
        
        private string departmentUUIDIdentifierField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Namespace="urn:oio:sd:snitflader:2011.12.01", Order=0)]
        public string DepartmentUUIDIdentifier
        {
            get
            {
                return this.departmentUUIDIdentifierField;
            }
            set
            {
                this.departmentUUIDIdentifierField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="urn:oio:sd:snitflader:2019.07.01")]
    public partial class GetDepartmentParent20190701Type
    {
        
        private GetDepartmentParentRequestType requestStructureField;
        
        private DepartmentParentType departmentParentField;
        
        private System.DateTime creationDateTimeField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(Order=0)]
        public GetDepartmentParentRequestType RequestStructure
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
        [System.Xml.Serialization.XmlElementAttribute(Order=1)]
        public DepartmentParentType DepartmentParent
        {
            get
            {
                return this.departmentParentField;
            }
            set
            {
                this.departmentParentField = value;
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
    public partial class GetDepartmentParent20190701OperationRequest
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="urn:oio:sd:snitflader:2019.07.01", Order=0)]
        public GetDepartmentParent.GetDepartmentParentRequestType GetDepartmentParent;
        
        public GetDepartmentParent20190701OperationRequest()
        {
        }
        
        public GetDepartmentParent20190701OperationRequest(GetDepartmentParent.GetDepartmentParentRequestType GetDepartmentParent)
        {
            this.GetDepartmentParent = GetDepartmentParent;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class GetDepartmentParent20190701OperationResponse
    {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Namespace="urn:oio:sd:snitflader:2019.07.01", Order=0)]
        public GetDepartmentParent.GetDepartmentParent20190701Type GetDepartmentParent20190701;
        
        public GetDepartmentParent20190701OperationResponse()
        {
        }
        
        public GetDepartmentParent20190701OperationResponse(GetDepartmentParent.GetDepartmentParent20190701Type GetDepartmentParent20190701)
        {
            this.GetDepartmentParent20190701 = GetDepartmentParent20190701;
        }
    }
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public interface GetDepartmentParent20190701PortTypeChannel : GetDepartmentParent.GetDepartmentParent20190701PortType, System.ServiceModel.IClientChannel
    {
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.Tools.ServiceModel.Svcutil", "2.0.2")]
    public partial class GetDepartmentParent20190701PortTypeClient : System.ServiceModel.ClientBase<GetDepartmentParent.GetDepartmentParent20190701PortType>, GetDepartmentParent.GetDepartmentParent20190701PortType
    {
        
        /// <summary>
        /// Implement this partial method to configure the service endpoint.
        /// </summary>
        /// <param name="serviceEndpoint">The endpoint to configure</param>
        /// <param name="clientCredentials">The client credentials</param>
        static partial void ConfigureEndpoint(System.ServiceModel.Description.ServiceEndpoint serviceEndpoint, System.ServiceModel.Description.ClientCredentials clientCredentials);
        
        public GetDepartmentParent20190701PortTypeClient() : 
                base(GetDepartmentParent20190701PortTypeClient.GetDefaultBinding(), GetDepartmentParent20190701PortTypeClient.GetDefaultEndpointAddress())
        {
            this.Endpoint.Name = EndpointConfiguration.GetDepartmentParent20190701.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetDepartmentParent20190701PortTypeClient(EndpointConfiguration endpointConfiguration) : 
                base(GetDepartmentParent20190701PortTypeClient.GetBindingForEndpoint(endpointConfiguration), GetDepartmentParent20190701PortTypeClient.GetEndpointAddress(endpointConfiguration))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetDepartmentParent20190701PortTypeClient(EndpointConfiguration endpointConfiguration, string remoteAddress) : 
                base(GetDepartmentParent20190701PortTypeClient.GetBindingForEndpoint(endpointConfiguration), new System.ServiceModel.EndpointAddress(remoteAddress))
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetDepartmentParent20190701PortTypeClient(EndpointConfiguration endpointConfiguration, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(GetDepartmentParent20190701PortTypeClient.GetBindingForEndpoint(endpointConfiguration), remoteAddress)
        {
            this.Endpoint.Name = endpointConfiguration.ToString();
            ConfigureEndpoint(this.Endpoint, this.ClientCredentials);
        }
        
        public GetDepartmentParent20190701PortTypeClient(System.ServiceModel.Channels.Binding binding, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(binding, remoteAddress)
        {
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        GetDepartmentParent.GetDepartmentParent20190701OperationResponse GetDepartmentParent.GetDepartmentParent20190701PortType.GetDepartmentParent20190701Operation(GetDepartmentParent.GetDepartmentParent20190701OperationRequest request)
        {
            return base.Channel.GetDepartmentParent20190701Operation(request);
        }
        
        public GetDepartmentParent.GetDepartmentParent20190701Type GetDepartmentParent20190701Operation(GetDepartmentParent.GetDepartmentParentRequestType GetDepartmentParent)
        {
            GetDepartmentParent.GetDepartmentParent20190701OperationRequest inValue = new GetDepartmentParent.GetDepartmentParent20190701OperationRequest();
            inValue.GetDepartmentParent = GetDepartmentParent;
            GetDepartmentParent.GetDepartmentParent20190701OperationResponse retVal = ((GetDepartmentParent.GetDepartmentParent20190701PortType)(this)).GetDepartmentParent20190701Operation(inValue);
            return retVal.GetDepartmentParent20190701;
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        System.Threading.Tasks.Task<GetDepartmentParent.GetDepartmentParent20190701OperationResponse> GetDepartmentParent.GetDepartmentParent20190701PortType.GetDepartmentParent20190701OperationAsync(GetDepartmentParent.GetDepartmentParent20190701OperationRequest request)
        {
            return base.Channel.GetDepartmentParent20190701OperationAsync(request);
        }
        
        public System.Threading.Tasks.Task<GetDepartmentParent.GetDepartmentParent20190701OperationResponse> GetDepartmentParent20190701OperationAsync(GetDepartmentParent.GetDepartmentParentRequestType GetDepartmentParent)
        {
            GetDepartmentParent.GetDepartmentParent20190701OperationRequest inValue = new GetDepartmentParent.GetDepartmentParent20190701OperationRequest();
            inValue.GetDepartmentParent = GetDepartmentParent;
            return ((GetDepartmentParent.GetDepartmentParent20190701PortType)(this)).GetDepartmentParent20190701OperationAsync(inValue);
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
            if ((endpointConfiguration == EndpointConfiguration.GetDepartmentParent20190701))
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
            if ((endpointConfiguration == EndpointConfiguration.GetDepartmentParent20190701))
            {
                return new System.ServiceModel.EndpointAddress("https://service.sd.dk/sdws/services/GetDepartmentParent20190701");
            }
            throw new System.InvalidOperationException(string.Format("Could not find endpoint with name \'{0}\'.", endpointConfiguration));
        }
        
        private static System.ServiceModel.Channels.Binding GetDefaultBinding()
        {
            return GetDepartmentParent20190701PortTypeClient.GetBindingForEndpoint(EndpointConfiguration.GetDepartmentParent20190701);
        }
        
        private static System.ServiceModel.EndpointAddress GetDefaultEndpointAddress()
        {
            return GetDepartmentParent20190701PortTypeClient.GetEndpointAddress(EndpointConfiguration.GetDepartmentParent20190701);
        }
        
        public enum EndpointConfiguration
        {
            
            GetDepartmentParent20190701,
        }
    }
}
