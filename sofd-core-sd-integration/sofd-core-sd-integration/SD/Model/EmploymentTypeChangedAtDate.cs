using System;
using System.Collections.Generic;
using System.Linq;

namespace GetEmploymentChangedAtDate
{
    public partial class EmploymentTypeChangedAtDate
    {
        public DepartmentTypeChangedAtDate DepartmentTypeChangedAtDate { 
            get 
            {
                return Items?.OfType<DepartmentTypeChangedAtDate>().Where(i => i.ActivationDate <= DateTime.Now || i.ActivationDate == EmploymentDate).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }
        public ProfessionTypeChangedAtDate ProfessionTypeChangedAtDate
        {
            get
            {
                return Items?.OfType<ProfessionTypeChangedAtDate>().Where(i => i.ActivationDate <= DateTime.Now || i.ActivationDate == EmploymentDate).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }
        public EmploymentStatusTypeChangedAtDate EmploymentStatusTypeChangedAtDate
        {
            get
            {
                // Employment Stop Date is a future change in Employment Status, so we CAN register this
                return Items?.OfType<EmploymentStatusTypeChangedAtDate>().OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }
        public SalaryAgreementTypeChangedAtDate SalaryAgreementTypeChangedAtDate
        {
            get
            {
                return Items?.OfType<SalaryAgreementTypeChangedAtDate>().Where(i => i.ActivationDate <= DateTime.Now || i.ActivationDate == EmploymentDate).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }
        public WorkingTimeTypeChangedAtDate WorkingTimeTypeChangedAtDate
        {
            get
            {
                return Items?.OfType<WorkingTimeTypeChangedAtDate>().Where(i => i.ActivationDate <= DateTime.Now || i.ActivationDate == EmploymentDate).OrderBy(i => i.ActivationDate).LastOrDefault();
            }
        }

    }
}
