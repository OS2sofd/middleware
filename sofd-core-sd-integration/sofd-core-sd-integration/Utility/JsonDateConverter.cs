using Newtonsoft.Json.Converters;

namespace DigitalIdentity.Utility
{
    public class JsonDateConverter : IsoDateTimeConverter
    {
        public JsonDateConverter(string format)
        {
            DateTimeFormat = format;
        }
    }
}
