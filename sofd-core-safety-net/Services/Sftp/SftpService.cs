using Microsoft.Extensions.Logging;
using Renci.SshNet;
using System;
using System.IO;

namespace sofd_core_safety_net.Services.Sftp
{
    internal class SftpService : ServiceBase<SftpService>
    {
        private readonly string username;
        private readonly string host;
        private readonly string password;
        private readonly int port;
        private readonly bool dryRun;
        private readonly string dryRunFilePath;

        public SftpService(IServiceProvider sp) : base(sp)
        {
            
            username = settings.SftpSettings.Username;
            host = settings.SftpSettings.Host;
            password = settings.SftpSettings.Password;
            port = settings.SftpSettings.Port;
            dryRun = settings.SftpSettings.DryRun;
            dryRunFilePath = settings.SftpSettings.DryRunFilePath;
        }

        public void UploadFile(MemoryStream memoryStream, string filename)
        {
            if (dryRun)
            {
                var name = filename;
                var path = dryRunFilePath + name;
                logger.LogInformation($"DryRun: uploading {filename}. Saving file to {path} because of DryRun.");
                FileStream outStream = new FileStream(path, FileMode.Create, FileAccess.Write);
                memoryStream.WriteTo(outStream);
                outStream.Flush();
                outStream.Close();
                memoryStream.Flush();
                memoryStream.Close();
            } else
            {
                try
                {
                    logger.LogInformation("Uploading file to Sftp server");
                    using var sftp = new SftpClient(host, port, username, password);
                    sftp.Connect();
                    sftp.UploadFile(memoryStream, filename);
                    sftp.Disconnect();
                    logger.LogInformation("Uploaded file to Sftp server");
                } catch (Exception ex)
                {
                    throw new Exception("Failed to upload file", ex);
                }
            }
        }
    }
}
