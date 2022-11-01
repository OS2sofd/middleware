using System;
using System.IO;
using System.Xml;
using System.Xml.Serialization;

namespace SofdSmsGateway
{
    [XmlRoot("sms")]
    public class SMS
    {
        [XmlElement(ElementName = "countrycode")]
        public string CountryCode { get; set; }

        [XmlElement(ElementName = "number")]
        public string Number { get; set; }

        [XmlElement(ElementName = "messageid")]
        public string MessageID { get; set; }

        [XmlElement(ElementName = "senderalias")]
        public string SenderAlias { get; set; }

        [XmlElement(ElementName = "shortcode")]
        public string ShortCode { get; set; }

        [XmlElement(ElementName = "message")]
        public string Message { get; set; }

        [XmlElement(ElementName = "encoding")]
        public string Encoding { get; set; }

        [XmlElement(ElementName = "category")]
        public string Category { get; set; }

        [XmlElement(ElementName = "category_description")]
        public string CategoryDescription { get; set; }

        [XmlElement(ElementName = "port")]
        public string Port { get; set; }

        [XmlElement(ElementName = "wapurl")]
        public string WAPUrl { get; set; }

        [XmlElement(ElementName = "flash")]
        public string Flash { get; set; }

        [XmlElement(ElementName = "price")]
        public string Price { get; set; }

        [XmlElement(ElementName = "donation")]
        public string Donation { get; set; }

        [XmlElement(ElementName = "callbackurl")]
        public string CallbackUrl { get; set; }

        [XmlElement(ElementName = "sendtiming")]
        public string SendTiming { get; set; }

        [XmlElement(ElementName = "bypassqueue")]
        public string BypassQueue { get; set; }
    }
}