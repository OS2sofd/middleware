using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Configuration;
using Renci.SshNet;
using Renci.SshNet.Sftp;

namespace SofdCprIntegration
{
    public class SFTPService
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private readonly IConfiguration _configuration;
        private const string HostName = "sftp.serviceplatformen.dk";
        private const string remoteDirectory = "IN";

        public SFTPService(IConfiguration configuration)
        {
            _configuration = configuration;
        }
        
        public List<FileDTO> GetNewestFiles(DateTime previousDate)
        {
            List<FileDTO> result = new List<FileDTO>();

            try
            {
                var pk = new PrivateKeyFile(_configuration["SFTPService:keyfile"], _configuration["SFTPService:keyPass"]);
                var keyFiles = new[] { pk };

                var methods = new List<AuthenticationMethod>();
                methods.Add(new PrivateKeyAuthenticationMethod(_configuration["SFTPService:username"], keyFiles));

                var connectionInfo = new ConnectionInfo(HostName, 22, _configuration["SFTPService:username"], methods.ToArray());


                using (var client = new SftpClient(connectionInfo))
                {
                    client.Connect();

                    var files = client.ListDirectory(remoteDirectory);

                    List<SftpFile> newestFiles = files.Where(f => ValidFile(f, previousDate)).OrderBy(f => f.LastWriteTime).ToList<SftpFile>();
                    if (newestFiles.Count > 0)
                    {
                        foreach(var newFile in newestFiles)
                        {
                            log.Info("Fetch file from FTP: " + newFile.FullName);

                            var fileDTO = new FileDTO();
                            fileDTO.Content = client.ReadAllText(newFile.FullName);
                            fileDTO.LastWriteTime = newFile.LastWriteTime;
                            result.Add(fileDTO);
                        }
                    }
                    else {
                        log.Info("Could not find any file matching criteria: " + _configuration["SFTPService:serviceAgreement"] + ", " + previousDate);
                    }

                    if (!_configuration.GetValue<Boolean>("SFTPService:deleteFiles"))
                    {
                        log.Info("Deletion of files disabled in config");
                    }
                    else
                    {
                        try
                        {
                            var oldFiles = files.Where(f => (DateTime.Now - f.LastWriteTime).TotalDays > 10).ToList();
                            if (oldFiles != null && oldFiles.Count > 0)
                            {
                                foreach (var oldFile in oldFiles)
                                {
                                    if (oldFile.IsDirectory || oldFile.Name.Equals(".") || oldFile.Name.Equals(".."))
                                    {
                                        continue;
                                    }
                                    log.Info("Deleting: " + oldFile.FullName);
                                    client.Delete(oldFile.FullName);
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            log.Error("Failed to delete old files", ex);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                log.Error("Failed to get file from FTP", ex);
            }

            return result;
        }

        private bool ValidFile(Renci.SshNet.Sftp.SftpFile f, DateTime previousDate)
        {
            return (
                !f.Name.Equals(".")
                && !f.Name.Equals("..")
                && !f.Name.EndsWith(".metadata")
                && f.Name.Contains(_configuration["SFTPService:serviceAgreement"])
                && f.LastWriteTime.CompareTo(previousDate) > 0
            );
        }
    }
}