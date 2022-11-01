using Serilog.Sinks.AwsCloudWatch;

namespace DigitalIdentity.CloudWatch
{
    public class LogStreamNameProvider : ILogStreamNameProvider
    {
        private readonly string name;
        public LogStreamNameProvider(string name) {
            this.name = name;
        }
        public string GetLogStreamName()
        {
            return name;
        }
    }
}
