using System.Text.RegularExpressions;

namespace DigitalIdentity.Utility
{
    public static class Helper
    {
        public static string FormatCprForLog(string cpr)
        {
            return Regex.Replace(cpr??"", ".{4}$", "xxxx");
        }
    }
}
