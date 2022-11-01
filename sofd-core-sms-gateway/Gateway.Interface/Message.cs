using System.Collections.Generic;
using System.Text;

namespace SofdSmsGateway
{
    public class Message
    {
        public string cvr { get; set; }
        public string content { get; set; }
        public List<string> numbers { get; set; }

        public override string ToString()
        {
            StringBuilder builder = new StringBuilder();
            builder.Append("{\"cvr\":");
            builder.Append(cvr);
            builder.Append(",\"content\":\"");
            builder.Append(content.Replace("\"", ""));
            builder.Append("\",\"phones\":[");

            bool first = true;
            foreach (var phone in numbers)
            {
                if (!first)
                {
                    builder.Append(",");
                }

                builder.Append("\"");
                builder.Append(phone);
                builder.Append("\"");

                first = false;
            }

            builder.Append("]}");

            return builder.ToString();
        }
    }
}
