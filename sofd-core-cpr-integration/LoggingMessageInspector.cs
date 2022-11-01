using System;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;
using System.Text;
using System.Xml;

namespace PersonBaseDataExtendedService
{
    internal class LoggingMessageInspector : IClientMessageInspector
    {

        public LoggingMessageInspector()
        {
        }

        public void AfterReceiveReply(ref Message reply, object correlationState)
        {
            Log(ref reply, "Response: ");
        }

        public object BeforeSendRequest(ref Message request, IClientChannel channel)
        {
            Log(ref request, "Request: ");

            return null;
        }

        private void Log(ref Message message, string prefix)
        {
            // copy into buffer (and recreate message, because it is lost when creating buffered copy)
            MessageBuffer buffer = message.CreateBufferedCopy(Int32.MaxValue);
            message = buffer.CreateMessage();

            // convert message to string
            Message msg = buffer.CreateMessage();
            StringBuilder sb = new StringBuilder();
            XmlWriter xw = XmlWriter.Create(sb);
            msg.WriteBody(xw);
            xw.Close();

            Console.WriteLine(prefix + sb.ToString());
        }
    }
}
