using System;
using System.Collections.Generic;
using System.Text;

namespace DigitalIdentity.S3
{
    public class S3Settings
    {
        public string Region { get; set; } = "eu-west-1";
        public string AWSAccessKeyId { get; set; }
        public string AWSSecretAccessKey { get; set; }
        public string BucketPath { get; set; }
    }
}
