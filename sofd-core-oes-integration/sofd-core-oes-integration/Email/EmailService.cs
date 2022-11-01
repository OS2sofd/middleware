using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Mail;

namespace DigitalIdentity.Email
{
    public class EmailService : BaseClass<EmailService>
    {
        private readonly SmtpClient smtpClient;

        public EmailService(IServiceProvider sp) : base(sp)
        {
            if (appSettings.EmailSettings.Enabled)
            {
                smtpClient = new SmtpClient()
                {
                    Host = appSettings.EmailSettings.Host,
                    Port = appSettings.EmailSettings.Port,
                    EnableSsl = true,
                    Credentials = new NetworkCredential()
                    {
                        UserName = appSettings.EmailSettings.UserName,
                        Password = appSettings.EmailSettings.Password
                    }
                };
            }
        }

        public void SendValidationErrors(List<string> errors)
        {
            if (errors.Count > 0)
            {
                var msg = new MailMessage();
                msg.To.Add(appSettings.EmailSettings.ToAddress);
                msg.From = new MailAddress(appSettings.EmailSettings.FromAddress);
                msg.Subject = "SOFD Core ØS integration - fejl";

                msg.Body = "Der er fundet følgende fejl i opmærkningen af enheder i SOFD:\n\n";
                errors.ForEach(e => msg.Body += e + "\n");

                if (!appSettings.EmailSettings.Enabled)
                {
                    logger.LogWarning($"EmailService is not enabled. Tried to send mail.\nTo: {msg.To}\nSubject: {msg.Subject}\nBody: {msg.Body}");
                    return;
                }
                try
                {
                    smtpClient.Send(msg);
                }
                catch (Exception e)
                {
                    logger.LogError(e, "Could not send email");
                }
            }
        }
    }
}