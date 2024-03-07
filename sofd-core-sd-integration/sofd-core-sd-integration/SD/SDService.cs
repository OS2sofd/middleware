using DigitalIdentity.SD.Model;
using DigitalIdentity.Utility;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Text.RegularExpressions;

namespace DigitalIdentity.SD
{
    class SDService : BaseClass<SDService>
    {
        private readonly SDServiceStubs sdServiceStubs;
        
        public SDService(IServiceProvider sp) : base(sp) {
            sdServiceStubs = sp.GetService<SDServiceStubs>();            
        }

        public List<SDPerson> GetPersons(string institutionIdentifier)
        {
            logger.LogDebug($"Getting all Persons from institution {institutionIdentifier}");
            var request = new GetPerson.GetPerson20111201OperationRequest();
            request.GetPerson = new GetPerson.GetPersonRequestType();
            request.GetPerson.InstitutionIdentifier = institutionIdentifier;
            request.GetPerson.StatusActiveIndicator = true;
            request.GetPerson.StatusPassiveIndicator = false;
            request.GetPerson.EffectiveDate = DateTime.Now;
            request.GetPerson.PostalAddressIndicator = true;
            var response = sdServiceStubs.GetPersonClient.GetPerson20111201Operation(request);
            var result = response.GetPerson20111201.Person.Select(p => SDPerson.FromPersonType(p)).ToList();
            return result;
        }
        public SDPerson GetPerson(string institutionIdentifier, string cpr)
        {
            logger.LogDebug($"Getting Person from institution {institutionIdentifier}, cpr {Helper.FormatCprForLog(cpr)}");
            try
            {
                var request = new GetPerson.GetPerson20111201OperationRequest();
                request.GetPerson = new GetPerson.GetPersonRequestType();
                request.GetPerson.InstitutionIdentifier = institutionIdentifier;
                request.GetPerson.PersonCivilRegistrationIdentifier = cpr;
                request.GetPerson.StatusActiveIndicator = true;
                request.GetPerson.StatusPassiveIndicator = false;
                request.GetPerson.EffectiveDate = DateTime.Now;
                request.GetPerson.PostalAddressIndicator = true;
                var response = sdServiceStubs.GetPersonClient.GetPerson20111201Operation(request);
                var person = response?.GetPerson20111201?.Person?.FirstOrDefault();
                if (person == null)
                {
                    throw new Exception("Person not found");
                }
                var result = SDPerson.FromPersonType(person);
                return result;
            }
            catch (Exception)
            {
                throw new Exception($"Person with InstitutionIdentifier {institutionIdentifier} and cpr {Helper.FormatCprForLog(cpr)} not found in SD");
            }
        }

        public List<SDPerson> GetAllEmployments(string institutionIdentifier, string employmentIdentifier, string cpr)
        {
            logger.LogDebug($"Getting Employments from institution {institutionIdentifier}, employmentIdentifier {employmentIdentifier}, cpr {Helper.FormatCprForLog(cpr)}");
            var request = new GetEmployment.GetEmploymentRequestType();
            request.InstitutionIdentifier = institutionIdentifier;
            request.EmploymentIdentifier = employmentIdentifier;
            request.PersonCivilRegistrationIdentifier = cpr;
            request.StatusActiveIndicator = true;
            request.StatusPassiveIndicator = true;
            request.EffectiveDate = DateTime.Now;
            request.DepartmentIndicator = true;                    // ”true” angiver at der ønskes afdelingsop-lysninger
            request.EmploymentStatusIndicator = true;              // ”true” angiver at der ønskes statusoplysninger
            request.ProfessionIndicator = true;                    // ”true” angiver at der ønskes stillingsoplysninger
            request.SalaryAgreementIndicator = true;               // ”true” angiver at der ønskes overens-komstoplysninger
            request.SalaryCodeGroupIndicator = false;              // ”true” angiver at der ønskes lønkode op-lysninger
            request.WorkingTimeIndicator = true;                   // ”true” angiver at der ønskes arbejdstidsoplysninger
            request.UUIDIndicator = true;                          // ”true” angiver at der ønskes UUID for afde-ling med i udtrækket

            var response = sdServiceStubs.GetEmploymentClient.GetEmployment20111201Operation(request);
            var result = response.Person == null ? new List<SDPerson>() : response.Person.Select(p => SDPerson.FromPersonType(p)).ToList();
            return result;
        }

        public List<SDPerson> GetChanges(string institutionIdentifier, DateTime changedFrom, DateTime changedTo, string cpr)
        {
            logger.LogDebug($"Getting Changes from institution {institutionIdentifier}, changedFrom: {changedFrom}, changedTo: {changedTo}");
            var request = new GetEmploymentChanged.GetEmploymentChangedRequestType();
            request.PersonCivilRegistrationIdentifier = cpr;
            request.InstitutionIdentifier = institutionIdentifier; // Entydig identifikation på en institution.
            request.ActivationDate = changedFrom.Date;             // Fradato for hvornår der ønskes ændringer.
            request.DeactivationDate = changedTo.Date.AddDays(1);  // Tildato for hvornår der ønskes ændringer.
            request.DepartmentIndicator = true;                    // ”true” angiver at der ønskes afdelingsop-lysninger
            request.EmploymentStatusIndicator = true;              // ”true” angiver at der ønskes statusoplysninger
            request.ProfessionIndicator = true;                    // ”true” angiver at der ønskes stillingsoplys-ninger
            request.SalaryAgreementIndicator = true;               // ”true” angiver at der ønskes overens-komstoplysninger
            request.SalaryCodeGroupIndicator = false;              // ”true” angiver at der ønskes lønkode op-lysninger
            request.WorkingTimeIndicator = true;                   // ”true” angiver at der ønskes arbejdstidsoplysninger
            request.UUIDIndicator = true;                          // ”true” angiver at der ønskes UUID for afde-ling med i udtrækket

            var changedEmployments = sdServiceStubs.GetEmploymentChangedClient.GetEmploymentChanged20111201Operation(request);
            var result = changedEmployments.Person == null ? new List<SDPerson>() : changedEmployments.Person.Select(p => SDPerson.FromPersonTypeChanged(p)).ToList();
            return result;
        }

        public List<SDPerson> GetChangesAtDate(string institutionIdentifier, DateTime changedFrom, DateTime changedTo, string cpr)
        {
            logger.LogDebug($"Getting Changes from institution {institutionIdentifier}, changedFrom: {changedFrom}, changedTo: {changedTo}");
            var request = new GetEmploymentChangedAtDate.GetEmploymentChangedAtDateRequestType();
            request.PersonCivilRegistrationIdentifier = cpr;
            request.InstitutionIdentifier = institutionIdentifier; // Entydig identifikation på en institution.
            request.ActivationDate = changedFrom.Date;             // Fradato for hvornår der ønskes ændringer.
            request.ActivationTime = changedFrom.Date;             // Angivelse af timer og minutter for hvornår der ønskes ændringer.
            request.DeactivationDate = changedTo.Date.AddDays(1);  // Tildato for hvornår der ønskes ændringer.
            request.DeactivationTime = changedTo.Date.AddDays(1);  // Angivelse af timer og minutter for hvornår der ønskes ændringer.
            request.DepartmentIndicator = true;                    // ”true” angiver at der ønskes afdelingsop-lysninger
            request.EmploymentStatusIndicator = true;              // ”true” angiver at der ønskes statusoplysninger
            request.ProfessionIndicator = true;                    // ”true” angiver at der ønskes stillingsoplys-ninger
            request.SalaryAgreementIndicator = true;               // ”true” angiver at der ønskes overens-komstoplysninger
            request.SalaryCodeGroupIndicator = false;              // ”true” angiver at der ønskes lønkode op-lysninger
            request.WorkingTimeIndicator = true;                   // ”true” angiver at der ønskes arbejdstidsoplysninger
            request.UUIDIndicator = true;                          // ”true” angiver at der ønskes UUID for afde-ling med i udtrækket
            request.FutureInformationIndicator = true;             // ”true” angiver at ved en ændring, ønskes efterfølgende datorelaterede oplysninger medtaget i XML-udtrækket.

            var changedEmployments = sdServiceStubs.GetEmploymentChangedAtDateClient.GetEmploymentChangedAtDate20111201Operation(request);
            var result = changedEmployments.Person == null ? new List<SDPerson>() : changedEmployments.Person.Select(p => SDPerson.FromPersonTypeChangedAtDate(p)).ToList();
            return result;
        }

        public List<SDOrgUnit> GetOrgUnits(string institutionIdentifier)
        {
            logger.LogDebug($"Getting all OrgUnits from SD institution {institutionIdentifier}");
            var request = new GetOrganization.GetOrganizationRequestType();
            request.InstitutionIdentifier = institutionIdentifier;
            request.ActivationDate = DateTime.Now;
            request.DeactivationDate = DateTime.Now;
            request.UUIDIndicator = true;
            var organization = sdServiceStubs.GetOrganizationClient.GetOrganization20111201Operation(request);

            var departmentRequest = new GetDepartment.GetDepartmentRequestType();
            departmentRequest.InstitutionIdentifier = institutionIdentifier;
            departmentRequest.ActivationDate = DateTime.Now;
            departmentRequest.DeactivationDate = DateTime.Now;
            departmentRequest.ContactInformationIndicator = false;
            departmentRequest.DepartmentNameIndicator = true;
            departmentRequest.EmploymentDepartmentIndicator = false;
            departmentRequest.PostalAddressIndicator = false;
            departmentRequest.ProductionUnitIndicator = false;
            departmentRequest.UUIDIndicator = true;
            var departments = sdServiceStubs.GetDepartmentClient.GetDepartment20111201Operation(departmentRequest);

            var orgUnitsDictionary = new Dictionary<string, SDOrgUnit>();
            AddDepartmentRecursive(orgUnitsDictionary, organization.Organization.First().DepartmentReference, departments);
            var orgUnits = orgUnitsDictionary.Values.ToList();
            return orgUnits;
        }

        private void AddDepartmentRecursive(Dictionary<string, SDOrgUnit> orgUnits, GetOrganization.DepartmentReferenceType[] departments, GetDepartment.GetDepartment20111201Type allDepartments)
        {
            foreach (var department in departments)
            {
                if (!orgUnits.ContainsKey(department.DepartmentUUIDIdentifier))
                {
                    var orgUnit = new SDOrgUnit();
                    orgUnit.Uuid = department.DepartmentUUIDIdentifier;
                    orgUnit.DepartmentIdentifier = department.DepartmentIdentifier.ToLower();
                    orgUnit.Name = allDepartments.Department.Where(d => d.DepartmentUUIDIdentifier == department.DepartmentUUIDIdentifier).FirstOrDefault()?.DepartmentName;
                    var parentDepartment = department.DepartmentReference?.FirstOrDefault();
                    if (parentDepartment != null)
                    {
                        orgUnit.ParentUuid = parentDepartment.DepartmentUUIDIdentifier;
                        var prefix = parentDepartment.DepartmentLevelIdentifier.Split("-").First();
                        orgUnit.ParentDepartmentIdentifier = $"{prefix}:{parentDepartment.DepartmentIdentifier}".ToLower();
                    }                                        
                    orgUnits.Add(orgUnit.Uuid, orgUnit);
                    if (orgUnit.ParentUuid != null)
                    {
                        AddDepartmentRecursive(orgUnits, department.DepartmentReference,allDepartments);
                    }
                }
            }
        }

        public List<FunkOrgEnhed> GetOrgFunctions()
        {
            try
            {
                var httpClient = new HttpClient();

                var byteArray = Encoding.ASCII.GetBytes($"{appSettings.SDSettings.Username}:{appSettings.SDSettings.Password}");
                httpClient.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Basic", Convert.ToBase64String(byteArray));

                var response = httpClient.GetAsync(new Uri(new Uri(appSettings.SDSettings.FunkBaseUrl), $"20160803/organisation/{appSettings.SDSettings.FunkTopInstUuid}/funktioner"));
                response.Wait();
                response.Result.EnsureSuccessStatusCode();
                var contents = response.Result.Content.ReadAsStringAsync();
                contents.Wait();
                var responseDto = JsonConvert.DeserializeObject<FunkFunktionerDto>(contents.Result);
                return responseDto.orgEnheder;
            }
            catch (Exception e)
            {
                throw new Exception($"Failed to get org functions", e);
            }
        }
    }
}
