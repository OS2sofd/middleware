using System.Xml.Serialization;

namespace SofdSmsGateway
{
    [XmlRoot("status")]
    public class Status
    {
        [XmlElement(ElementName = "statusline")]
        public Statusline[] Statusline { get; set; }
    }

    public class Statusline
    {
        [XmlElement(ElementName = "code")]
        public string Code { get; set; }
        
        [XmlElement(ElementName = "description")]
        public string Description { get; set; }
    }

}
