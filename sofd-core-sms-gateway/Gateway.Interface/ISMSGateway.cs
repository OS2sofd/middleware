namespace SofdSmsGateway
{
    public interface ISMSGateway
    {
        bool SendSMS(Message message);
    }
}