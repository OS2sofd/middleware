using System.Linq;

namespace GetEmployment
{
    public partial class EmploymentType
    {
        public EmploymentDepartmentType EmploymentDepartmentType
        {
            get
            {
                return Items?.OfType<EmploymentDepartmentType>().LastOrDefault();
            }
        }
        public ProfessionType ProfessionType
        {
            get
            {
                return Items?.OfType<ProfessionType>().LastOrDefault();
            }
        }
        public EmploymentStatusType EmploymentStatusType
        {
            get
            {
                return Items?.OfType<EmploymentStatusType>().LastOrDefault();
            }
        }
        public SalaryAgreementType SalaryAgreementType
        {
            get
            {
                return Items?.OfType<SalaryAgreementType>().LastOrDefault();
            }
        }
        public WorkingTimeType WorkingTimeType
        {
            get
            {
                return Items?.OfType<WorkingTimeType>().LastOrDefault();
            }
        }

    }
}
