using System;
using System.Net.Http;
using System.Text;

namespace SofdSmsGateway
{
    public class ComputopicGateway : ISMSGateway
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private const string RequestUrl = "https://smssys.dk/sms";
        private string username;
        private string password;

        public ComputopicGateway(string username, string password)
        {
            this.username = username;
            this.password = password;
        }

        public bool SendSMS(Message message)
        {
            SMS sms = new SMS();
            sms.CountryCode = "45";
            sms.Message = message.content;
            sms.Number = string.Join(",", message.numbers);

            var status = SendSMS(sms);
            if (status == null || status.Statusline.Length == 0)
            {
                log.Error("Unable to send message for: " + message.cvr + ". Message: " + message.ToString());
                return false;
            }

            bool success = false;

            foreach (var statusline in status.Statusline) {
                if ("0".Equals(statusline?.Code))
                {
                    // as long as one message was send, we are happy
                    success = true;
                }
                else
                {
                    log.Warn("Failure: " + statusline.Code + " / " + statusline.Description);
                }
            }

            return success;
        }

        private void InitCheck()
        {
            if (string.IsNullOrEmpty(username) || string.IsNullOrEmpty(password))
            {
                throw new SystemException("ComputopicGateway is not properly configured.");
            }
        }

        private Status SendSMS(SMS sms)
        {
            InitCheck();

            using (HttpClient client = new HttpClient())
            {
                var byteArray = Encoding.UTF8.GetBytes($"{username}:{password}");
                client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Basic", Convert.ToBase64String(byteArray));
                client.DefaultRequestHeaders.Add("Accept", "application/xml");

                var httpContent = new StringContent(sms.Serialize(), Encoding.UTF8, "application/xml");

                using (HttpResponseMessage response = client.PostAsync(RequestUrl, httpContent).Result)
                {
                    using (HttpContent content = response.Content)
                    {
                        string result = content.ReadAsStringAsync().Result;

                        return result.XmlDeserializeFromString<Status>();
                    }
                }
            }
        }
    }
}
