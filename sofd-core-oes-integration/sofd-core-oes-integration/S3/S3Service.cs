using Amazon;
using Amazon.S3;
using Amazon.S3.Transfer;
using Microsoft.Extensions.Logging;
using System;
using System.IO;

namespace DigitalIdentity.S3
{
    class S3Service : BaseClass<S3Service>
    {
        private readonly IAmazonS3 amazonS3;

        public S3Service(IServiceProvider sp) : base(sp)
        {
            var region = RegionEndpoint.GetBySystemName(appSettings.S3Settings.Region);
            amazonS3 = new AmazonS3Client(appSettings.S3Settings.AWSAccessKeyId, appSettings.S3Settings.AWSSecretAccessKey, region);
        }

        public void UploadFile(string bucketName, string fileName, Stream fileStream)
        {
            try
            {
                logger.LogDebug("Uploading file to S3");
                var fileTransferUtility = new TransferUtility(amazonS3);
                fileTransferUtility.Upload(fileStream, bucketName, fileName);
                logger.LogDebug("Uploaded file to S3");
            }
            catch (Exception e)
            {
                throw new Exception("Failed to upload file", e);
            }
        }
    }
}
