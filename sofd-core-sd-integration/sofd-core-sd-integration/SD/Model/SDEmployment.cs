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

        public static SDEmployment FromEmploymentTypeChanged(GetEmploymentChanged.EmploymentType e, bool isNewAffiliation)
        {
            var result = new SDEmployment();
            result.EmploymentIdentifier = e.EmploymentIdentifier;
            result.AnniversaryDate = e.AnniversaryDate.Year == 1 ? null : (DateTime?)e.AnniversaryDate;

            // for new affiliations we accept any future data, so that we can get them registered in sofd ahead of time
            if (isNewAffiliation)
            {
                result.DepartmentUUIDIdentifier = e.MostRecentDepartmentChange?.DepartmentUUIDIdentifier ?? e.NearestFutureDepartmentChange?.DepartmentUUIDIdentifier;
                result.JobPositionIdentifier = e.MostRecentProfessionChange?.JobPositionIdentifier ?? e.NearestFutureProfessionChange?.JobPositionIdentifier;
                result.EmploymentName = e.MostRecentProfessionChange?.EmploymentName ?? e.NearestFutureProfessionChange?.EmploymentName;
                result.EmploymentStatusCode = e.MostRecentEmploymentStatusChange?.EmploymentStatusCode ?? e.NearestFutureEmploymentStatusChange?.EmploymentStatusCode;
                result.EmploymentStatusText = GetEmploymentStatusText(e.MostRecentEmploymentStatusChange?.EmploymentStatusCode ?? e.NearestFutureEmploymentStatusChange?.EmploymentStatusCode);
                result.SalaryClassIdentifier = e.MostRecentSalaryAgreementChange?.SalaryClassIdentifier ?? e.NearestFutureSalaryAgreementChange?.SalaryClassIdentifier;
                result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?)e.EmploymentDate;
                var statusChange = e.MostRecentEmploymentStatusChange ?? e.NearestFutureEmploymentStatusChange;
                result.StopDate = (statusChange != null, CalculateStopDate(statusChange?.EmploymentStatusCode, statusChange?.ActivationDate));
                result.OccupationRate = e.MostRecentWorkingTimeChange?.OccupationRate ?? e.NearestFutureWorkingTimeChange?.OccupationRate;
                result.SalariedIndicator = e.MostRecentWorkingTimeChange?.SalariedIndicator ?? e.NearestFutureWorkingTimeChange?.SalariedIndicator;
                result.PrepaidIndicator = e.MostRecentSalaryAgreementChange?.PrepaidIndicator ?? e.NearestFutureSalaryAgreementChange?.PrepaidIndicator;
            }
            // for existing affiliations we're only interested in the future stopdate - otherwise we only look at most recent change
            else 
            {
                result.DepartmentUUIDIdentifier = e.MostRecentDepartmentChange?.DepartmentUUIDIdentifier;
                result.JobPositionIdentifier = e.MostRecentProfessionChange?.JobPositionIdentifier;
                result.EmploymentName = e.MostRecentProfessionChange?.EmploymentName;
                result.EmploymentStatusCode = e.MostRecentEmploymentStatusChange?.EmploymentStatusCode;
                result.EmploymentStatusText = GetEmploymentStatusText(e.MostRecentEmploymentStatusChange?.EmploymentStatusCode);
                result.SalaryClassIdentifier = e.MostRecentSalaryAgreementChange?.SalaryClassIdentifier;
                result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?)e.EmploymentDate;
                var statusChange = e.MostRecentEmploymentStatusChange ?? e.NearestFutureEmploymentStatusChange;
                result.StopDate = (statusChange != null, CalculateStopDate(statusChange?.EmploymentStatusCode, statusChange?.ActivationDate));
                result.OccupationRate = e.MostRecentWorkingTimeChange?.OccupationRate;
                result.SalariedIndicator = e.MostRecentWorkingTimeChange?.SalariedIndicator;
                result.PrepaidIndicator = e.MostRecentSalaryAgreementChange?.PrepaidIndicator;
            }
            return result;
        }

        public static SDEmployment FromEmploymentTypeChangedAtDate(GetEmploymentChangedAtDate.EmploymentTypeChangedAtDate e)
        {
            var result = new SDEmployment();
            result.EmploymentIdentifier = e.EmploymentIdentifier;
            result.AnniversaryDate = e.AnniversaryDate.Year == 1 ? null : (DateTime?)e.AnniversaryDate;
            result.StartDate = (e.EmploymentDate == DateTime.MinValue) ? null : (DateTime?)e.EmploymentDate;

            var departmentChange = e.Items?.OfType<DepartmentTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault();
            result.DepartmentUUIDIdentifier = departmentChange?.DepartmentUUIDIdentifier;

            var professionChange = e.Items?.OfType<ProfessionTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault();
            result.JobPositionIdentifier = professionChange?.JobPositionIdentifier;
            result.EmploymentName = professionChange ?.EmploymentName;

            var employmentStatusChange = e.Items?.OfType<EmploymentStatusTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault();
            result.EmploymentStatusCode = employmentStatusChange?.EmploymentStatusCode;
            result.EmploymentStatusText = GetEmploymentStatusText(result.EmploymentStatusCode);
            result.StopDate = (employmentStatusChange != null, CalculateStopDate(employmentStatusChange?.EmploymentStatusCode, employmentStatusChange?.ActivationDate));

            var salaryChange = e.Items?.OfType<SalaryAgreementTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault();
            result.SalaryClassIdentifier = salaryChange?.SalaryClassIdentifier;
            result.PrepaidIndicator = salaryChange?.PrepaidIndicator;

            var workingTimeChange = e.Items?.OfType<WorkingTimeTypeChangedAtDate>().Where(i => i.ActivationDate >= e.EmploymentDate).OrderBy(i => i.ActivationDate).FirstOrDefault();
            result.OccupationRate = workingTimeChange?.OccupationRate;
            result.SalariedIndicator = workingTimeChange?.SalariedIndicator;
            return result;
        }
    }
}