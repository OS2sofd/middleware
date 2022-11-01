using GetEmploymentChangedAtDate;
using System;
using System.Linq;

namespace DigitalIdentity.SD.Model
{
    public class SDEmployment
    {
        public string EmploymentIdentifier { get; set; }
        public DateTime? AnniversaryDate { get; set; }
        public string DepartmentUUIDIdentifier { get; set; }
        public string JobPositionIdentifier { get; set; }
        public string EmploymentName { get; set; }
        public string EmploymentStatusCode { private get; set; }
        public string EmploymentStatusText { private get; set; }
        public string SalaryClassIdentifier { get; set; }
        public DateTime? StartDate { get; set; }
        // using a simple tuple datatype to keep track of the difference between a "patch" with no changes to stopdate and a "patch" with a stopdate that has been set to "null".
        public (bool Changed, DateTime? Value) StopDate { get; set; }
        public decimal? OccupationRate { get; set; }
        public bool? SalariedIndicator { get; set; }
        public bool? PrepaidIndicator { get; set; }

        public string GetOPUSEmploymentTermsId()
        {
            // we code some logic to match OPUS values using SD data
            // might change later when more fields are added to SOFD Core
            if (SalariedIndicator == true && PrepaidIndicator == true)
            {
                return "00";
            }
            if (SalariedIndicator == true && PrepaidIndicator == false)
            {
                return "01";
            }
            if (SalariedIndicator == false)
            {
                return "03";
            }
            return null;
        }

        public string GetOPUSEmploymentTermsText()
        {
            return (GetOPUSEmploymentTermsId()) switch
            {
                "00" => "Månedsløn forud",
                "01" => "Månedsløn bagud",
                "03" => "Måneds-/timeløn",
                _ => null,
            };
        }

        public bool IsDeleted()
        {
            return EmploymentStatusCode == "S";
        }

        public static string GetEmploymentStatusText(string code)
        {
            return code switch
            {
                null => null,
                "0" => "Ansat, men ikke sat i løn",
                "1" => "Ansat og sat i løn",
                "3" => "Ansat, men midlertidig ude af løn",
                "7" => "Emigreret eller død",
                "8" => "Fratrådt",
                "9" => "Pensioneret",
                "S" => "Ansættelsesforholdet er slettet",
                _ => "Ukendt",
            };
        }

        private static DateTime? CalculateStopDate(string code, DateTime? activationDate)
        {
            // 0,1,3 are the 3 "Ansat" codes
            //"0" => "Ansat, men ikke sat i løn",
            //"1" => "Ansat og sat i løn",
            //"3" => "Ansat, men midlertidig ude af løn",
            if (!(code == "0" || code == "1" || code == "3"))
            {
                return activationDate;
            }
            return null;
        }

        public static SDEmployment FromEmploymentType(GetEmployment.EmploymentType e)
        {
            var result = new SDEmployment();
            result.EmploymentIdentifier = e.EmploymentIdentifier;
            result.AnniversaryDate = e.AnniversaryDate.Year == 1 ? null : (DateTime?)e.AnniversaryDate;
            result.DepartmentUUIDIdentifier = e.EmploymentDepartmentType?.DepartmentUUIDIdentifier;
            result.JobPositionIdentifier = e.ProfessionType?.JobPositionIdentifier;
            result.EmploymentName = e.ProfessionType?.EmploymentName;
            if (result.EmploymentName == null && result.JobPositionIdentifier != null)
            {
                // SOFD doesn't support null position name
                result.EmploymentName = "";
            }
            result.EmploymentStatusCode = e.EmploymentStatusType?.EmploymentStatusCode;
            result.EmploymentStatusText = GetEmploymentStatusText(e.EmploymentStatusType?.EmploymentStatusCode);
            result.SalaryClassIdentifier = e.SalaryAgreementType?.SalaryClassIdentifier;
            result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?) e.EmploymentDate;
            result.StopDate = (e.EmploymentStatusType != null, CalculateStopDate(e.EmploymentStatusType?.EmploymentStatusCode, e.EmploymentStatusType?.ActivationDate));
            result.OccupationRate = e.WorkingTimeType?.OccupationRate;
            result.SalariedIndicator = e.WorkingTimeType?.SalariedIndicator;
            result.PrepaidIndicator = e.SalaryAgreementType?.PrepaidIndicator;
            return result;
        }

        public static SDEmployment FromEmploymentTypeChanged(GetEmploymentChanged.EmploymentType e)
        {
            var result = new SDEmployment();
            result.EmploymentIdentifier = e.EmploymentIdentifier;
            result.AnniversaryDate = e.AnniversaryDate.Year == 1 ? null : (DateTime?)e.AnniversaryDate;
            result.DepartmentUUIDIdentifier = e.EmploymentDepartmentType?.DepartmentUUIDIdentifier;
            result.JobPositionIdentifier = e.ProfessionType?.JobPositionIdentifier;
            result.EmploymentName = e.ProfessionType?.EmploymentName;
            result.EmploymentStatusCode = e.EmploymentStatusType?.EmploymentStatusCode;
            result.EmploymentStatusText = GetEmploymentStatusText(e.EmploymentStatusType?.EmploymentStatusCode);
            result.SalaryClassIdentifier = e.SalaryAgreementType?.SalaryClassIdentifier;
            result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?)e.EmploymentDate;
            result.StopDate = (e.EmploymentStatusType != null, CalculateStopDate(e.EmploymentStatusType?.EmploymentStatusCode, e.EmploymentStatusType?.ActivationDate));
            result.OccupationRate = e.WorkingTimeType?.OccupationRate;
            result.SalariedIndicator = e.WorkingTimeType?.SalariedIndicator;
            result.PrepaidIndicator = e.SalaryAgreementType?.PrepaidIndicator;

            return result;
        }

        public static SDEmployment FromEmploymentTypeChangedAtDate(GetEmploymentChangedAtDate.EmploymentTypeChangedAtDate e)
        {
            var result = new SDEmployment();
            result.EmploymentIdentifier = e.EmploymentIdentifier;
            result.AnniversaryDate = e.AnniversaryDate.Year == 1 ? null : (DateTime?)e.AnniversaryDate;
            result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?)e.EmploymentDate;
            result.DepartmentUUIDIdentifier = e.DepartmentTypeChangedAtDate?.DepartmentUUIDIdentifier;
            result.JobPositionIdentifier = e.ProfessionTypeChangedAtDate?.JobPositionIdentifier;
            result.EmploymentName = e.ProfessionTypeChangedAtDate?.EmploymentName;
            result.EmploymentStatusCode = e.Items?.OfType<EmploymentStatusTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault()?.EmploymentStatusCode;
            result.EmploymentStatusText = GetEmploymentStatusText(result.EmploymentStatusCode);
            result.SalaryClassIdentifier = e.SalaryAgreementTypeChangedAtDate?.SalaryClassIdentifier;
            result.StopDate = (e.EmploymentStatusTypeChangedAtDate != null, CalculateStopDate(e.EmploymentStatusTypeChangedAtDate?.EmploymentStatusCode, e.EmploymentStatusTypeChangedAtDate?.ActivationDate));
            result.OccupationRate = e.WorkingTimeTypeChangedAtDate?.OccupationRate;
            result.SalariedIndicator = e.WorkingTimeTypeChangedAtDate?.SalariedIndicator;
            result.PrepaidIndicator = e.SalaryAgreementTypeChangedAtDate?.PrepaidIndicator;
            return result;
        }
    }
}