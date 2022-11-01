using System.Text.RegularExpressions;

namespace sofd_core_oes_integration.Utility
{
    public static class StringExtentions
    {
        public static string Truncate(this string value, int maxLength)
        {
            if (string.IsNullOrEmpty(value)) return value;
            return value.Length <= maxLength ? value : value.Substring(0, maxLength);
        }

        public static string FormatCprForLog(this string value)
        {
            return Regex.Replace(value ?? "", ".{4}$", "xxxx");
        }

    }
}
