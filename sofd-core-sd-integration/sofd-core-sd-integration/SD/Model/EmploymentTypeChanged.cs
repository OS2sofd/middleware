using System;
using System.Linq;

namespace GetEmploymentChanged
{
    public partial class EmploymentType
    {
        public EmploymentDepartmentType MostRecentDepartmentChange
        { 
            get 
            {
                return Items?.OfType<EmploymentDepartmentType>().Where(i => i.ActivationDate <= DateTime.Now).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

        public EmploymentDepartmentType NearestFutureDepartmentChange
        {
            get
            {
                return Items?.OfType<EmploymentDepartmentType>().Where(i => i.ActivationDate > DateTime.Now).OrderBy(i => i.ActivationDate).FirstOrDefault();
            }
        }

        public ProfessionType MostRecentProfessionChange
        {
            get
            {
                return Items?.OfType<ProfessionType>().Where(i => i.ActivationDate <= DateTime.Now).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

        public ProfessionType NearestFutureProfessionChange
        {
            get
            {
                return Items?.OfType<ProfessionType>().Where(i => i.ActivationDate > DateTime.Now).OrderBy(i => i.ActivationDate).FirstOrDefault();
            }
        }

        public EmploymentStatusType MostRecentEmploymentStatusChange
        {
            get
            {
                return Items?.OfType<EmploymentStatusType>().Where(i => i.ActivationDate <= DateTime.Now).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

        public EmploymentStatusType NearestFutureEmploymentStatusChange
        {
            get
            {
                return Items?.OfType<EmploymentStatusType>().Where(i => i.ActivationDate > DateTime.Now).OrderBy(i => i.ActivationDate).FirstOrDefault();
            }
        }

        public SalaryAgreementType MostRecentSalaryAgreementChange
        {
            get
            {
                return Items?.OfType<SalaryAgreementType>().Where(i => i.ActivationDate <= DateTime.Now).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

        public SalaryAgreementType NearestFutureSalaryAgreementChange
        {
            get
            {
                return Items?.OfType<SalaryAgreementType>().Where(i => i.ActivationDate > DateTime.Now).OrderBy(i => i.ActivationDate).FirstOrDefault();
            }
        }

        public WorkingTimeType MostRecentWorkingTimeChange
        {
            get
            {
                return Items?.OfType<WorkingTimeType>().Where(i => i.ActivationDate <= DateTime.Now).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

        public WorkingTimeType NearestFutureWorkingTimeChange
        {
            get
            {
                return Items?.OfType<WorkingTimeType>().Where(i => i.ActivationDate > DateTime.Now).OrderBy(i => i.ActivationDate).FirstOrDefault();
            }
        }
    }
}
