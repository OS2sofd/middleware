namespace DigitalIdentity.SOFD
{
    public class SOFDSettings
    {
        public string BaseUrl { get; set; }
        public string ApiKey { get; set; }
        public int GetPersonsPageSize { get; set; }
        public int GetPersonsPageCount { get; set; }
        public int GetOrgUnitsPageSize { get; set; }
        public string MasterPrefix { get; set; }
    }
}
