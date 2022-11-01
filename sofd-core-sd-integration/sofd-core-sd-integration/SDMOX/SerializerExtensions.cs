using System.IO;
using System.Xml;
using System.Xml.Serialization;

namespace DigitalIdentity.SDMOX
{
    public static class SerializerExtensions
    {
        public static string ToXml<T>(this T toSerialize)
        {
            var ns = new XmlSerializerNamespaces();
            ns.Add("oio", "urn:oio:definitions:1.0.0");
            ns.Add("sd", "urn:oio:sagdok:3.0.0");
            ns.Add("orgfaelles", "urn:oio:sagdok:organisation:2.0.0");
            ns.Add("silkdata", "urn:oio:silkdata:1.0.0");
            ns.Add("cvr", "http://rep.oio.dk/cvr.dk/xml/schemas/2005/03/22/");
            ns.Add("dkcc1", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/");
            ns.Add("dkcc2", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/03/15/");
            ns.Add("itst1", "http://rep.oio.dk/itst.dk/xml/schemas/2005/06/24/");
            ns.Add("sd20070301", "http://rep.oio.dk/sd.dk/xml.schema/20070301/");

            var xmlSerializer = new XmlSerializer(toSerialize.GetType());
            using var textWriter = new StringWriter();
            xmlSerializer.Serialize(textWriter, toSerialize,ns);
            return textWriter.ToString();
        }

        public static XmlElement ToXmlElement<T>(this T toSerialize)
        {
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(toSerialize.ToXml());
            return doc.DocumentElement;
        }
    }
}