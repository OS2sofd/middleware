using Serilog.Sinks.AwsCloudWatch;

namespace sofd_core_safety_net.Amazon
{
    public class LogStreamNameProvider : ILogStreamNameProvider
    {
        private readonly string name;
        public LogStreamNameProvider(string name)
        {
            this.name = name;
        }
        public string GetLogStreamName()
        {
            return name;
        }
    }
}
