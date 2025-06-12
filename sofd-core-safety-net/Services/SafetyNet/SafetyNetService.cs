using Microsoft.Extensions.Logging;
using sofd_core_safety_net.SafetyNet.Model;
using sofd_core_safety_net.Services.SafetyNet.Model;
using sofd_core_safety_net.Services.Sofd.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace sofd_core_safety_net.Services.SafetyNet
{
    internal class SafetyNetService : ServiceBase<SafetyNetService>
    {
        public SafetyNetService(IServiceProvider sp) : base(sp)
        {
        }

        public string GenerateCSVOrgUnits(List<OrgUnit> sofdOrgUnits)
        {
            try
            {
                logger.LogDebug("Generating Safety Net OrgUnit CSV");

                var safetyNetOrgUnits = ConvertOrgUnits(sofdOrgUnits);
                var sb = new StringBuilder();

                // headers
                sb.Append($"Afdelingskode;");
                sb.Append($"Afdelingsnavn;");
                sb.Append($"Overafdelingskode;");
                sb.Append($"VirksomhedsP.Nr;");
                sb.Append($"Virksomhedsnavn");
                sb.Append(Environment.NewLine);

                // lines
                foreach (var safetyNetOrgUnit in safetyNetOrgUnits)
                {
                    sb.Append($"{safetyNetOrgUnit.Id};");          // Afdelingskode INT NOT NULL
                    sb.Append($"{safetyNetOrgUnit.Name};");           // Afdelingsnavn STRING(max) NOT NULL
                    sb.Append($"{safetyNetOrgUnit.ParentId};");    // Overafdelingskode INT NOT NULL
                    sb.Append($"{safetyNetOrgUnit.Pnr};");            // VirksomhedsP.Nr STRING(max) NULL
                    sb.Append($"{safetyNetOrgUnit.TopOrgUnitName}"); // Virksomhedsnavn STRING(max) NULL
                    sb.Append(Environment.NewLine);
                }
                return sb.ToString();
            }
            catch (Exception e)
            {
                throw new Exception("Failed to Generate Safety Net OrgUnit CSV", e);
            }
        }

        public string GenerateCSVPersons(List<Person> sofdPersons, List<OrgUnit> sofdOrgUnits)
        {
            try
            {
                logger.LogDebug("Generating Safety Net Person CSV");

                var employees = ConvertPersons(sofdPersons, sofdOrgUnits);
                var sb = new StringBuilder();

                // headers
                sb.Append($"Medarbejdernr;");
                sb.Append($"CPR-nr;");
                sb.Append($"Fornavn;");
                sb.Append($"Efternavn;");
                sb.Append($"Afdelingskode;");
                sb.Append($"ErLeder;");
                sb.Append($"Fastbopæl Vejnavn;");
                sb.Append($"Husnr;");
                sb.Append($"Postnummer;");
                sb.Append($"Telefonnummer;");
                sb.Append($"Primær email;");
                sb.Append($"AD-brugernavn;");
                sb.Append($"Dato for 1.tiltræd.;");
                sb.Append($"Fratrædelsesdato;");
                sb.Append($"Stilling;");
                sb.Append($"Ledelsesniveau;");
                sb.Append($"Leders MedarbejderNr;");
                sb.Append($"ErAMR;");
                sb.Append($"ErHMU;");
                sb.Append($"ErOMU;");
                sb.Append($"ErLMU;");
                sb.Append($"ErDS;");
                sb.Append($"ErTR;");
                sb.Append($"ErFTR");

                sb.Append(Environment.NewLine);

                // lines
                foreach (var employee in employees)
                {
                    sb.Append($"{employee.OpusId};");
                    sb.Append($"{employee.Cpr};");
                    sb.Append($"{(employee.Firstname == null ? "" : employee.Firstname.Replace(";", ""))};");
                    sb.Append($"{(employee.Surname == null ? "" : employee.Surname.Replace(";", ""))};");
                    sb.Append($"{employee.OrgUnitLosId};");
                    sb.Append($"{(employee.IsManager ? 1 : 0)};");
                    sb.Append($"{(employee.Street == null ? "" : employee.Street.Replace(";", ""))};");
                    sb.Append($"{(employee.StreetNumber == null ? "" : employee.StreetNumber.Replace(";", ""))};");
                    sb.Append($"{(employee.PostalCode == null ? "" : employee.PostalCode.Replace(";", ""))};");
                    sb.Append($"{(employee.PhoneNumber == null ? "" : employee.PhoneNumber.Replace(";",""))};");
                    sb.Append($"{(employee.PrimaryEmail == null ? "" : employee.PrimaryEmail.Replace(";", ""))};");
                    sb.Append($"{(employee.PrimaryADUsername == null ? "" : employee.PrimaryADUsername.Replace(";", ""))};");
                    sb.Append($"{(employee.StartDate == null ? "" : employee.StartDate.Value.ToString("yyyy-MM-dd"))};");
                    sb.Append($"{(employee.StopDate == null ? "" : employee.StopDate.Value.ToString("yyyy-MM-dd"))};");
                    sb.Append($"{(employee.Position == null ? "" : employee.Position.Replace(";", ""))};");
                    sb.Append($"{employee.ManagerLevel};");
                    sb.Append($"{employee.ManagerOpusId};");
                    sb.Append($"{(employee.IsAMR ? 1 : 0)};");
                    sb.Append($"{(employee.IsHMU ? 1 : 0)};");
                    sb.Append($"{(employee.IsOMU ? 1 : 0)};");
                    sb.Append($"{(employee.IsLMU ? 1 : 0)};");
                    sb.Append($"{(employee.IsDS ? 1 : 0)};");
                    sb.Append($"{(employee.IsTR ? 1 : 0)};");
                    sb.Append($"{(employee.IsFTR ? 1 : 0)}");

                    sb.Append(Environment.NewLine);
                }
                return sb.ToString();
            }
            catch (Exception e)
            {
                throw new Exception("Failed to Generate Safety Net Person CSV", e);
            }
        }


        private List<SafetyNetOrgUnit> ConvertOrgUnits(List<OrgUnit> sofdOrgUnits)
        {
            var rootUnit = sofdOrgUnits.Where(o => o.ParentUuid == null).Single();
            var orgunits = new List<SafetyNetOrgUnit>();
            foreach (var sofdOrgUnit in sofdOrgUnits)
            {
                var orgunit = new SafetyNetOrgUnit();
                if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.OPUS)
                {
                    orgunit.Id = int.Parse(sofdOrgUnit.MasterId); // LOS ID for OPUS org units
                }
                else if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.SOFD)
                {
                    orgunit.Id = sofdOrgUnit.Id;
                }
                else
                {
                    throw new Exception("Unknown mastermode");
                }
                
                orgunit.Name = sofdOrgUnit.Name.Replace(";", "");
                orgunit.Pnr = sofdOrgUnit.Pnr == null ? null : sofdOrgUnit.Pnr.Replace(",", "");

                if (sofdOrgUnit.ParentUuid != null)
                {
                    var parent = sofdOrgUnits.Where(o => o.Uuid.Equals(sofdOrgUnit.ParentUuid)).Single();
                    if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.OPUS)
                    {                        
                        orgunit.ParentId = int.Parse(parent.MasterId);
                    }
                    else if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.SOFD)
                    {
                        orgunit.ParentId = parent.Id;
                    }
                    else
                    {
                        throw new Exception("Unknown mastermode");
                    }                    
                }

                orgunit.TopOrgUnitName = rootUnit.Name;

                orgunits.Add(orgunit);
            }
            return orgunits;
        }

        private (String name, String number) SplitAddressOnNumber(string address)
        {
            String name = "";
            String number = "";
            if (address != null)
            {
                var index = address.IndexOfAny("0123456789".ToCharArray());
                if (index <= 0)
                {
                    name = address;
                }
                else
                {
                    name = address.Substring(0, index - 1).Trim();
                    number = address.Substring(index, address.Length - index).Trim();
                }
            }
            return (name, number);
        }

        private List<Employee> ConvertPersons(List<Person> sofdPersons, List<OrgUnit> sofdOrgUnits)
        {
            var employees = new List<Employee>();
            Dictionary<string, int> orgUnitsWithLevel = GetOrgUnitsWithLevel(sofdOrgUnits);
            foreach (var person in sofdPersons)
            {
                var affiliations = person.Affiliations.Where(a => 
                    a.EmployeeId != null 
                    && a.Master == "OPUS" 
                    && (a.StartDate == null || a.StartDate <= DateTime.Now.Date)
                    && (a.StopDate == null || a.StopDate >= DateTime.Now.Date));

                foreach (var affiliation in affiliations) {
                    var affiliationOrgUnit = sofdOrgUnits.Where(o => o.Uuid.Equals(affiliation.OrgunitUuid)).FirstOrDefault();
                    if (affiliationOrgUnit == null)
                    {
                        logger.LogWarning($"Affiliation with EmployeeId {affiliation.EmployeeId}. No affiliation OrgUnit found");
                        continue;
                    }

                    var affiliationOrgUnitPost = affiliationOrgUnit.PostAddresses.Where(a => a.Prime).FirstOrDefault();
                    var manager = sofdPersons.Where(p => p.Uuid.Equals(affiliationOrgUnit.Manager?.Uuid)).FirstOrDefault();
                    var managerPrimaryAffiliation = manager == null ? null : manager.Affiliations.Where(a => a.Prime).FirstOrDefault();

                    var employee = new Employee();
                    employee.OpusId = int.Parse(affiliation.EmployeeId);
                    employee.Cpr = person.Cpr;
                    employee.Firstname = person.Firstname;
                    employee.Surname = person.Surname;

                    if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.OPUS)
                    {
                        employee.OrgUnitLosId = int.Parse(affiliationOrgUnit.MasterId);
                    }
                    else if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.SOFD)
                    {
                        employee.OrgUnitLosId = affiliationOrgUnit.Id;
                    }
                    else
                    {
                        throw new Exception("Unknown mastermode");
                    }
                    
                    employee.IsManager = person.Uuid == affiliationOrgUnit.Manager?.Uuid;
                    (String streetName, String streetNumber) = SplitAddressOnNumber(person.RegisteredPostAddress?.Street);
                    employee.Street = streetName;
                    employee.PhoneNumber = person.Phones?.Where(p => p.Prime).FirstOrDefault()?.PhoneNumber;
                    employee.StreetNumber = streetNumber;
                    employee.PostalCode = person.RegisteredPostAddress?.PostalCode;
                    employee.StartDate = settings.SofdSettings.UseAffiliationStartDate ? affiliation.StartDate : person.FirstEmploymentDate;
                    employee.StopDate = affiliation.StopDate;
                    employee.Position = affiliation.PositionName;
                    employee.ManagerLevel = orgUnitsWithLevel[affiliationOrgUnit.Uuid];
                    employee.ManagerOpusId = manager != null 
                        && managerPrimaryAffiliation != null
                        && managerPrimaryAffiliation.Master == "OPUS"
                        && managerPrimaryAffiliation.EmployeeId != null ? int.Parse(managerPrimaryAffiliation.EmployeeId) : 0;                    
                    employee.IsAMR = affiliation.Functions.Any(s => s.Equals("AMR"));
                    employee.IsHMU = affiliation.Functions.Any(s => s.Equals("HMU"));
                    employee.IsOMU = affiliation.Functions.Any(s => s.Equals("OMU"));
                    employee.IsLMU = affiliation.Functions.Any(s => s.Equals("LMU"));
                    employee.IsDS = affiliation.Functions.Any(s => s.Equals("Daglig sikkerhedsleder"));
                    employee.IsTR = affiliation.Functions.Any(s => s.Equals("TR"));
                    employee.IsFTR = affiliation.Functions.Any(s => s.Equals("FTR"));

                    var adUser = person.Users.Where(u => 
                        (u.UserType == "ACTIVE_DIRECTORY" || u.UserType == "ACTIVE_DIRECTORY_SCHOOL")
                        && u.EmployeeId == affiliation.EmployeeId).FirstOrDefault();
                    if (adUser == null)
                    {
                        adUser = person.Users.Where(u => u.UserType == "ACTIVE_DIRECTORY" && u.Prime && String.IsNullOrEmpty(u.EmployeeId)).FirstOrDefault();
                    }
                    if (adUser == null)
                    {
                        adUser = person.Users.Where(u => u.UserType == "ACTIVE_DIRECTORY_SCHOOL" && u.Prime && String.IsNullOrEmpty(u.EmployeeId)).FirstOrDefault();
                    }
                    if (adUser != null)
                    {
                        employee.PrimaryADUsername = adUser.UserId;
                        User emailUser = null;
                        if (adUser.UserType == "ACTIVE_DIRECTORY")
                        {
                            emailUser = person.Users.Where(u => u.UserType.Equals("EXCHANGE") && u.MasterId == adUser.UserId).FirstOrDefault();
                        }
                        if (adUser.UserType == "ACTIVE_DIRECTORY_SCHOOL")
                        {
                            emailUser = person.Users.Where(u => u.UserType.Equals("SCHOOL_EMAIL") && u.MasterId == adUser.UserId).FirstOrDefault();
                        }
                        if (emailUser != null)
                        {
                            employee.PrimaryEmail = emailUser.UserId;
                        }
                    }
                    employees.Add(employee);
                }
            }
            return employees;
        }

        private Dictionary<string, int> GetOrgUnitsWithLevel(List<OrgUnit> sofdOrgUnits)
        {
            // Dictionary<OrgUnitUuid, level>
            Dictionary<string, int> result = new Dictionary<string, int>();
            OrgUnit parent = sofdOrgUnits.Where(o => o.ParentUuid == null).First();
            GetChildrenRecursive(sofdOrgUnits, result, parent, 1);

            return result;
        }

        private void GetChildrenRecursive(List<OrgUnit> allOus, Dictionary<string, int> result, OrgUnit currentNode, int currentLevel)
        {
            result[currentNode.Uuid] = currentLevel;

            foreach (OrgUnit childOu in allOus.Where(o => o.ParentUuid != null && o.ParentUuid.Equals(currentNode.Uuid)).ToList())
            {
                GetChildrenRecursive(allOus, result, childOu, currentLevel + 1);
            }
        }
    }
}