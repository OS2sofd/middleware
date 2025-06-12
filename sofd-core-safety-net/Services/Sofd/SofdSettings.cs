using System.Collections.Generic;

namespace sofd_core_safety_net.Sofd
{
    public class SofdSettings
    {
        public string Url { get; set; }
        public string ApiKey { get; set; }
        public long PersonsPageSize { get; set; }
        public long PersonsPageCount { get; set; }
        public long OrgUnitPageSize { get; set; }
        public List<string> OrgUnitBlackList { get; set; } = new List<string>();
        public string PositionNameExcludeRegex { get; set; }
        public bool UseAffiliationStartDate { get; set; } = false;
        public MasterModeType MasterMode { get; set; } = MasterModeType.OPUS;

        public enum MasterModeType
        {
            OPUS,SOFD
        }
    }
}
