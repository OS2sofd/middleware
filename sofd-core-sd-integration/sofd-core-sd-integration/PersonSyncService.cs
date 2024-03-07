using DigitalIdentity;
using DigitalIdentity.SD;
using DigitalIdentity.SD.Model;
using DigitalIdentity.SOFD;
using DigitalIdentity.SOFD.Model;
using DigitalIdentity.Utility;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using sofd_core_sd_integration.Database;
using sofd_core_sd_integration.Database.Model;
using sofd_core_sd_integration.SOFD.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using static DigitalIdentity.SD.SDSettings;

namespace sofd_core_sd_integration
{
    public class PersonSyncService : BaseClass<PersonSyncService>
    {
        private readonly SDService sdService;
        private readonly SOFDService sofdService;
        private readonly DatabaseContext databaseContext;
        private readonly List<SDPerson> fullSDPersons = new List<SDPerson>();
        private List<OrgUnit> sofdOrgUnits;
        private Dictionary<string, string> sofdOrgUnitTags = new Dictionary<string, string>();
        private List<Person> sofdPersons;


        public PersonSyncService(IServiceProvider sp) : base(sp)
        {
            sdService = sp.GetService<SDService>();
            sofdService = sp.GetService<SOFDService>();
            databaseContext = sp.GetService<DatabaseContext>();
        }

        public void Synchronize()
        {
            try
            {
                if (!appSettings.EmployeeSyncEnabled)
                {
                    logger.LogWarning("PersonSyncService not enabled");
                    return;
                }
                logger.LogInformation("PersonSyncService executing");
                sofdOrgUnits = sofdService.GetOrgUnits();
                if (appSettings.EmployeeSyncUseTags)
                {
                    // create lookup dictionary for orgunit tags
                    foreach (var sofdOrgUnit in sofdOrgUnits)
                    {
                        var tag = sofdOrgUnit.Tags.Where(t => t.Tag == appSettings.EmployeeSyncTagName).FirstOrDefault();
                        if (tag != null)
                        {
                            foreach (var tagValue in tag.CustomValue.Split(","))
                            {
                                sofdOrgUnitTags[tagValue.ToLower()] = sofdOrgUnit.Uuid;
                            }
                        }

                    }
                }
                SynchronizePersonsToSOFD();
                if (appSettings.FunctionSyncEnabled)
                {
                    SynchronizeFunctionsToSOFD();
                }
                logger.LogInformation("PersonSyncService finsihed");
            }
            catch (Exception e)
            {
                logger.LogError(e, "Unhandled exception");
            }
        }

        private void SynchronizeFunctionsToSOFD()
        {
            var sdOrgFunctions = sdService.GetOrgFunctions();
            sofdPersons = sofdService.GetPersons();
            var dbOrgUnits = databaseContext.DBOrgUnits.ToArray();
            var managerMap = new Dictionary<string, Manager>();
            foreach (var sdOrgFunction in sdOrgFunctions)
            {
                var sofdUuid = dbOrgUnits.Where(o => o.VirtualUuid == sdOrgFunction.uuid).Select(o => o.SofdUuid).SingleOrDefault();
                if (sofdUuid == null)
                {
                    continue;
                }
                var managerPerson = sdOrgFunction.orgFunktioner.Where(of => of.klasseNavn == "SD - Leder" && of.start <= DateTime.Now.Date && of.slut >= DateTime.Now.Date).FirstOrDefault()?.personer.FirstOrDefault();
                if (managerPerson != null)
                {
                    var sofdPerson = sofdPersons.Where(p => p.Cpr == managerPerson.brugervendtNoegle).FirstOrDefault();
                    if (sofdPerson == null)
                    {
                        logger.LogWarning($"Manager {managerPerson.navn} ({Helper.FormatCprForLog(managerPerson.brugervendtNoegle)}) for {sdOrgFunction.navn} could not be found in SOFD");
                        continue;
                    }
                    var manager = new Manager();
                    manager.Name = sofdPerson.GetName();
                    manager.Uuid = sofdPerson.Uuid;
                    manager.Inherited = false;
                    managerMap.Add(sofdUuid, manager);
                }
            }
            var sofdRoot = sofdOrgUnits.Where(o => o.ParentUuid == null).Single();
            AddManagerRecursive(sofdRoot, managerMap, null);
        }

        private void AddManagerRecursive(OrgUnit parent, Dictionary<string, Manager> managerMap, Manager parentManager)
        {
            managerMap.TryGetValue(parent.Uuid, out var sdManager);
            if (sdManager == null && parentManager != null)
            {
                sdManager = parentManager;
                sdManager.Inherited = true;
            }
            if (parent.Manager != null && sdManager == null)
            {
                logger.LogDebug($"Deleting manager for {parent.Name}");
                sofdService.ClearManager(parent);
            }
            else if (!Object.Equals(parent.Manager, sdManager))
            {
                logger.LogDebug($"Updating manager on {parent.Name}: {sdManager.Name}");
                var orgUnitWithManager = new OrgUnit();
                orgUnitWithManager.Uuid = parent.Uuid;
                orgUnitWithManager.Manager = sdManager;
                sofdService.UpdateOrgUnit(orgUnitWithManager);
            }
            foreach (var child in sofdOrgUnits.Where(o => o.ParentUuid == parent.Uuid))
            {
                AddManagerRecursive(child, managerMap, sdManager);
            }
        }

        private void SynchronizePersonsToSOFD()
        {
            // handle manually added fullsync persons
            var fullSyncPersons = databaseContext.FullSyncPersons;
            if (fullSyncPersons.Count() > 0)
            {
                sofdPersons = sofdService.GetPersons();
                foreach (var person in databaseContext.FullSyncPersons)
                {
                    foreach (var institution in appSettings.SDSettings.Institutions)
                    {
                        List<SDOrgUnit> sdOrgUnits = null;
                        if (!institution.Prime || appSettings.EmployeeSyncUseTags ) {
                            sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                        }
                        logger.LogInformation($"Performing Manual Full Sync For cpr {Helper.FormatCprForLog(person.Cpr)} in institution {institution.Identifier}");
                        FullSynchronizeForInstitution(institution, sdOrgUnits, person.Cpr);
                        logger.LogInformation($"Performing Manual Future Delta Sync For cpr {Helper.FormatCprForLog(person.Cpr)} in institution {institution.Identifier}");
                        DeltaSynchronizeForInstitution(institution, sdOrgUnits, DateTime.Now.AddDays(-30), DateTime.Now, true, person.Cpr);
                    }
                    databaseContext.FullSyncPersons.Remove(person);
                }
                databaseContext.SaveChanges();
            }

            // handle normal sync routine
            var syncInfo = databaseContext.SynchronizeInfo.SingleOrDefault();
            var isFullSync = syncInfo == null;
            if (isFullSync)
            {
                sofdPersons = sofdService.GetPersons();
                syncInfo = new SynchronizeInfo { PersonsLastSync = DateTime.Now };
                databaseContext.Add(syncInfo);
                foreach (var institution in appSettings.SDSettings.Institutions)
                {
                    List<SDOrgUnit> sdOrgUnits = null;
                    if (!institution.Prime || appSettings.EmployeeSyncUseTags)
                    {
                        sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                    }
                    fullSDPersons.AddRange(sdService.GetPersons(institution.Identifier));
                    logger.LogInformation($"Performing Full Sync for institution {institution}");
                    FullSynchronizeForInstitution(institution,sdOrgUnits);
                }
            }
            else
            {
                var changedFrom = syncInfo.PersonsLastSync;
                var changedTo = DateTime.Now;

                foreach (var institution in appSettings.SDSettings.Institutions)
                {
                    List<SDOrgUnit> sdOrgUnits = null;
                    if (!institution.Prime || appSettings.EmployeeSyncUseTags)
                    {
                        sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                    }
                    logger.LogInformation($"Performing Delta Sync for institution {institution.Identifier}");
                    DeltaSynchronizeForInstitution(institution,sdOrgUnits, changedFrom, changedTo,false, null);
                    logger.LogInformation($"Performing Future Delta Sync for institution {institution.Identifier}");
                    DeltaSynchronizeForInstitution(institution,sdOrgUnits, changedFrom, changedTo,true, null);
                }
                syncInfo.PersonsLastSync = changedTo;
            }
            databaseContext.SaveChanges();
        }

        private void FullSynchronizeForInstitution(Institution institution, List<SDOrgUnit> sdOrgUnits, string cpr = null)
        {
            try
            {
                var master = appSettings.SOFDSettings.MasterPrefix + institution.Identifier;
                var sdPersons = sdService.GetAllEmployments(institution.Identifier, null, cpr);
                var missingSDOrgUnitUUIDs = new HashSet<string>();
                foreach (var sdPerson in sdPersons)
                {
                    try
                    {
                        var sofdPerson = sofdPersons.Where(p => p.Cpr == sdPerson.PersonCivilRegistrationIdentifier).SingleOrDefault();
                        bool isExistingSOFDPerson = sofdPerson != null;
                        if (isExistingSOFDPerson)
                        {
                            sofdPerson.TrackChanges();
                        }
                        else
                        {
                            // get the full person details from SD
                            var fullSDPerson = fullSDPersons.Where(p => p.PersonCivilRegistrationIdentifier == sdPerson.PersonCivilRegistrationIdentifier).FirstOrDefault();
                            if (fullSDPerson == null)
                            {
                                fullSDPerson = sdService.GetPerson(institution.Identifier, sdPerson.PersonCivilRegistrationIdentifier);
                            }
                            sofdPerson = new Person();
                            sofdPerson.Master = master;
                            sofdPerson.Cpr = fullSDPerson.PersonCivilRegistrationIdentifier;
                            sofdPerson.Firstname = fullSDPerson.PersonGivenName;
                            sofdPerson.Surname = fullSDPerson.PersonSurnameName;
                            if (sdPerson.HasValidAddress())
                            {
                                sofdPerson.RegisteredPostAddress = new PostAddress
                                {
                                    Master = master,
                                    MasterId = master,
                                    AddressProtected = false,
                                    City = fullSDPerson.City,
                                    Street = fullSDPerson.Street,
                                    Country = fullSDPerson.Country,
                                    PostalCode = fullSDPerson.PostalCode
                                };
                            }
                        }
                        foreach (var sdEmployment in sdPerson.Employments)
                        {
                            // if instititution is not the prime institution, we prefix the employmentidentifier to prevent duplicates
                            var employmentMasterId = institution.Prime ? sdEmployment.EmploymentIdentifier : institution.Identifier + "-" + sdEmployment.EmploymentIdentifier;

                            var sofdAffiliation = sofdPerson.Affiliations.Where(e => e.Master == master && e.MasterId == employmentMasterId).SingleOrDefault();
                            if (sofdAffiliation == null)
                            {
                                sofdAffiliation = new Affiliation();
                                sofdPerson.Affiliations.Add(sofdAffiliation);
                                sofdAffiliation.Uuid = Guid.NewGuid().ToString();
                                sofdAffiliation.Master = master;
                                sofdAffiliation.MasterId = employmentMasterId;
                                sofdAffiliation.AffiliationType = "EMPLOYEE";
                                sofdAffiliation.EmployeeId = employmentMasterId;
                            }
                            sofdPerson.AnniversaryDate = sdEmployment.AnniversaryDate;
                            // set first employment as lowest employmentdate
                            if (sofdPerson.FirstEmploymentDate == null || sofdPerson.FirstEmploymentDate > sdEmployment.StartDate)
                            {
                                sofdPerson.FirstEmploymentDate = sdEmployment.StartDate;
                            }
                            sofdAffiliation.PositionId = sdEmployment.JobPositionIdentifier;
                            sofdAffiliation.PositionName = String.IsNullOrEmpty(sdEmployment.EmploymentName) ? "Ukendt" : sdEmployment.EmploymentName;
                            sofdAffiliation.EmploymentTerms = sdEmployment.GetOPUSEmploymentTermsId();
                            sofdAffiliation.EmploymentTermsText = sdEmployment.GetOPUSEmploymentTermsText();
                            sofdAffiliation.PayGrade = sdEmployment.SalaryClassIdentifier;
                            sofdAffiliation.StartDate = sdEmployment.StartDate;
                            sofdAffiliation.StopDate = sdEmployment.StopDate.Value;
                            var occupationHours = appSettings.DefaultOccupationHours;
                            if (appSettings.PositionOccupationHoursMap.ContainsKey(sofdAffiliation.PositionId))
                            {
                                occupationHours = appSettings.PositionOccupationHoursMap[sofdAffiliation.PositionId];
                            }
                            sofdAffiliation.WorkingHoursNumerator = Math.Round(occupationHours * (sdEmployment.OccupationRate ?? 1), 2);
                            sofdAffiliation.WorkingHoursDenominator = occupationHours;
                            if (appSettings.EmployeeSyncUseTags)
                            {
                                // setting orgunit uuid based on Tags in SOFD core
                                var sdOrgunit = sdOrgUnits.Where(o => o.Uuid == sdEmployment.DepartmentUUIDIdentifier).First();
                                // first try to look up nuværende afdeling in a SOFD tag
                                if (sofdOrgUnitTags.ContainsKey(sdOrgunit.DepartmentIdentifier))
                                {
                                    sofdAffiliation.OrgUnitUuid = sofdOrgUnitTags[sdOrgunit.DepartmentIdentifier];
                                }
                                // then try to look up parent NY afdeling in a SOFD tag
                                else if (sofdOrgUnitTags.ContainsKey(sdOrgunit.ParentDepartmentIdentifier))
                                {
                                    sofdAffiliation.OrgUnitUuid = sofdOrgUnitTags[sdOrgunit.ParentDepartmentIdentifier];
                                }
                                else
                                {
                                    missingSDOrgUnitUUIDs.Add(sdOrgunit.Uuid);
                                    logger.LogInformation($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because no tag was found in SOFD.");                                    
                                    sofdPerson.Affiliations.Remove(sofdAffiliation);
                                    continue;
                                }
                            }
                            else
                            {
                                // setting orgunit uuid to the same af department uuid in SD
                                if (institution.Prime)
                                {
                                    sofdAffiliation.OrgUnitUuid = sdEmployment.DepartmentUUIDIdentifier;
                                }
                                else
                                {
                                    // if this is not the prime sd institution then apply org mappings
                                    string mappedUuid = null;
                                    var sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == sdEmployment.DepartmentUUIDIdentifier).FirstOrDefault();
                                    while (sdOrgUnit != null && mappedUuid == null)
                                    {
                                        // check if sd orgunit is mapped
                                        var mapping = institution.Mappings.Where(m => m.SDUuid == sdOrgUnit.Uuid).FirstOrDefault();
                                        if (mapping != null)
                                        {
                                            mappedUuid = mapping.SOFDUuid;
                                        }
                                        else
                                        {
                                            sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == sdOrgUnit.ParentUuid).FirstOrDefault();
                                        }
                                    }
                                    if (mappedUuid != null)
                                    {
                                        sofdAffiliation.OrgUnitUuid = mappedUuid;
                                    }
                                    else
                                    {
                                        missingSDOrgUnitUUIDs.Add(sdOrgUnit.Uuid);
                                        logger.LogInformation($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because SD orgunit {sdEmployment.DepartmentUUIDIdentifier} was not mapped to a SOFD orgunit.");
                                        sofdPerson.Affiliations.Remove(sofdAffiliation);
                                        continue;
                                    }
                                }
                            }
                            if (!sofdOrgUnits.Any(ou => ou.Uuid == sofdAffiliation.OrgUnitUuid))
                            {
                                missingSDOrgUnitUUIDs.Add(sdEmployment.DepartmentUUIDIdentifier);
                                logger.LogInformation($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because orgunit {sofdAffiliation.OrgUnitUuid} was not found in SOFD.");
                                sofdPerson.Affiliations.Remove(sofdAffiliation);
                                continue;
                            }
                            if (sdEmployment.IsDeleted())
                            {
                                logger.LogInformation($"Deleting affiliation {sofdAffiliation.PositionName}, {sofdAffiliation.EmployeeId} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)}");
                                sofdPerson.Affiliations.Remove(sofdAffiliation);
                                continue;
                            }
                        }

                        // delete Affiliations not found in SD
                        sofdPerson.Affiliations.RemoveAll(sofd => sofd.Master == master && !sdPerson.Employments.Select(sd => institution.Prime ? sd.EmploymentIdentifier : institution.Identifier + "-" + sd.EmploymentIdentifier).Contains(sofd.MasterId));
                        if (!isExistingSOFDPerson)
                        {
                            sofdService.CreatePerson(sofdPerson);
                        }
                        else if (sofdPerson.IsChanged())
                        {
                            sofdService.UpdatePerson(sofdPerson);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.LogWarning(e, $"Failed to sync person with Cpr {Helper.FormatCprForLog(sdPerson.PersonCivilRegistrationIdentifier)}");
                        databaseContext.Add(new FailedSyncPerson(sdPerson.PersonCivilRegistrationIdentifier, e.Message));
                    }
                }
                var toBeDeleted = sofdPersons.Where(p => p.Cpr == cpr && sdPersons.Count == 0).ToList();
                if (cpr == null)
                {
                    // handle any SofdPersons with SD affiliations that no longer exist in SD at all
                    toBeDeleted.AddRange(sofdPersons.Where(p => !sdPersons.Any(sdp => sdp.PersonCivilRegistrationIdentifier == p.Cpr) && p.Affiliations.Any(a => a.Master == master)));
                }
                foreach (var sofdPerson in toBeDeleted)
                {
                    logger.LogInformation($"Removing all SD affiliations with master {master} from person with Uuid {sofdPerson.Uuid}");
                    sofdPerson.Affiliations.RemoveAll(a => a.Master == master);
                    sofdService.UpdatePerson(sofdPerson);
                }
                NotifyAboutMissingOrgs(sdOrgUnits, missingSDOrgUnitUUIDs);
            }
            catch (Exception e)
            {
                logger.LogWarning(e, $"Could not get employments from SD {institution.Identifier} cpr: {Helper.FormatCprForLog(cpr)}");
            }
        }

        private void DeltaSynchronizeForInstitution(Institution institution, List<SDOrgUnit> sdOrgUnits, DateTime changedFrom, DateTime changedTo, bool isFuture, string cpr)
        {
            try
            {
                var master = appSettings.SOFDSettings.MasterPrefix + institution.Identifier;

                var changedPersons = isFuture ? sdService.GetChangesAtDate(institution.Identifier, changedFrom, changedTo, cpr) : sdService.GetChanges(institution.Identifier, changedFrom, changedTo, cpr);
                var missingTagLogs = new List<string>();
                var missingSDOrgUnitUUIDs = new HashSet<string>();
                foreach (var changedPerson in changedPersons)
                {
                    try
                    {
                        var sofdPerson = sofdService.GetPerson(changedPerson.PersonCivilRegistrationIdentifier);

                        bool isExistingSOFDPerson = sofdPerson != null;
                        if (isExistingSOFDPerson)
                        {
                            sofdPerson.TrackChanges();
                        }
                        else
                        {
                            // SOFD doesn't know this person - get the full person record from SD
                            var sdPerson = sdService.GetPerson(institution.Identifier, changedPerson.PersonCivilRegistrationIdentifier);
                            sofdPerson = new Person();
                            sofdPerson.Master = master;
                            sofdPerson.Cpr = sdPerson.PersonCivilRegistrationIdentifier;
                            sofdPerson.Firstname = sdPerson.PersonGivenName;
                            sofdPerson.Surname = sdPerson.PersonSurnameName;
                            // do not add address from SD if it looks funky (propably address protected)
                            if (sdPerson.HasValidAddress())
                            {
                                sofdPerson.RegisteredPostAddress = new PostAddress
                                {
                                    Master = master,
                                    MasterId = master,
                                    AddressProtected = false,
                                    City = sdPerson.City,
                                    Street = sdPerson.Street,
                                    Country = sdPerson.Country,
                                    PostalCode = sdPerson.PostalCode
                                };
                            }
                        }

                        foreach (var changedEmployment in changedPerson.Employments)
                        {

                            // if instititution is not the prime institution, we prefix the employmentidentifier to prevent duplicates
                            var employmentMasterId = institution.Prime ? changedEmployment.EmploymentIdentifier : institution.Identifier + "-" + changedEmployment.EmploymentIdentifier;

                            var sofdAffiliation = sofdPerson.Affiliations.Where(e => e.Master == master && e.MasterId == employmentMasterId).SingleOrDefault();
                            bool isNewAffilition = false;
                            if (sofdAffiliation == null)
                            {
                                isNewAffilition = true;
                                sofdAffiliation = new Affiliation();
                                sofdAffiliation.Uuid = Guid.NewGuid().ToString();
                                sofdAffiliation.Master = master;
                                sofdAffiliation.MasterId = employmentMasterId;
                                sofdAffiliation.AffiliationType = "EMPLOYEE";
                                sofdAffiliation.EmployeeId = employmentMasterId;
                            }
                            sofdPerson.AnniversaryDate = changedEmployment.AnniversaryDate ?? sofdPerson.AnniversaryDate;
                            // set first employment as lowest employmentdate
                            if (changedEmployment.StartDate != null && (sofdPerson.FirstEmploymentDate == null || sofdPerson.FirstEmploymentDate > changedEmployment.StartDate))
                            {
                                sofdPerson.FirstEmploymentDate = changedEmployment.StartDate;
                            }
                            sofdAffiliation.PositionId = changedEmployment.JobPositionIdentifier ?? sofdAffiliation.PositionId;
                            if (changedEmployment.EmploymentName != null || isNewAffilition)
                            {
                                sofdAffiliation.PositionName = String.IsNullOrEmpty(changedEmployment.EmploymentName) ? "Ukendt" : changedEmployment.EmploymentName;
                            }
                            sofdAffiliation.EmploymentTerms = changedEmployment.GetOPUSEmploymentTermsId();
                            sofdAffiliation.EmploymentTermsText = changedEmployment.GetOPUSEmploymentTermsText();
                            sofdAffiliation.PayGrade = changedEmployment.SalaryClassIdentifier ?? sofdAffiliation.PayGrade;
                            sofdAffiliation.StartDate = changedEmployment.StartDate ?? sofdAffiliation.StartDate;
                            sofdAffiliation.StopDate = changedEmployment.StopDate.Changed ? changedEmployment.StopDate.Value : sofdAffiliation.StopDate;
                            if (changedEmployment.OccupationRate != null)
                            {
                                var occupationHours = appSettings.DefaultOccupationHours;
                                if (appSettings.PositionOccupationHoursMap.ContainsKey(sofdAffiliation.PositionId))
                                {
                                    occupationHours = appSettings.PositionOccupationHoursMap[sofdAffiliation.PositionId];
                                }
                                sofdAffiliation.WorkingHoursNumerator = Math.Round(occupationHours * (changedEmployment.OccupationRate ?? 1), 2);
                                sofdAffiliation.WorkingHoursDenominator = occupationHours;
                            }
                            if (changedEmployment.DepartmentUUIDIdentifier != null)
                            {
                                if (appSettings.EmployeeSyncUseTags)
                                {
                                    // setting orgunit uuid based on Tags in SOFD core
                                    var sdOrgunit = sdOrgUnits.Where(o => o.Uuid == changedEmployment.DepartmentUUIDIdentifier).First();
                                    // first try to look up nuværende afdeling in a SOFD tag
                                    if (sofdOrgUnitTags.ContainsKey(sdOrgunit.DepartmentIdentifier))
                                    {
                                        sofdAffiliation.OrgUnitUuid = sofdOrgUnitTags[sdOrgunit.DepartmentIdentifier];
                                    }
                                    // then try to look up parent NY afdeling in a SOFD tag
                                    else if (sofdOrgUnitTags.ContainsKey(sdOrgunit.ParentDepartmentIdentifier))
                                    {
                                        sofdAffiliation.OrgUnitUuid = sofdOrgUnitTags[sdOrgunit.ParentDepartmentIdentifier];
                                    }
                                    else
                                    {
                                        missingSDOrgUnitUUIDs.Add(sdOrgunit.Uuid);
                                        missingTagLogs.Add($"{sdOrgunit.ParentDepartmentIdentifier} -> {sdOrgunit.DepartmentIdentifier}");
                                        logger.LogInformation($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because no tag was found in SOFD.");
                                        sofdPerson.Affiliations.Remove(sofdAffiliation);
                                        continue;
                                    }
                                }
                                else
                                {
                                    // setting orgunit uuid to the same af department uuid in SD
                                    if (institution.Prime)
                                    {
                                        sofdAffiliation.OrgUnitUuid = changedEmployment.DepartmentUUIDIdentifier;
                                    }
                                    else
                                    {
                                        // if this is not the prime sd institution then apply org mappings
                                        string mappedUuid = null;
                                        var sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == changedEmployment.DepartmentUUIDIdentifier).FirstOrDefault();
                                        while (sdOrgUnit != null && mappedUuid == null)
                                        {
                                            // check if sd orgunit is mapped
                                            var mapping = institution.Mappings.Where(m => m.SDUuid == sdOrgUnit.Uuid).FirstOrDefault();
                                            if (mapping != null)
                                            {
                                                mappedUuid = mapping.SOFDUuid;
                                            }
                                            else
                                            {
                                                sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == sdOrgUnit.ParentUuid).FirstOrDefault();
                                            }
                                        }
                                        if (mappedUuid != null)
                                        {
                                            sofdAffiliation.OrgUnitUuid = mappedUuid;
                                        }
                                        else
                                        {
                                            missingSDOrgUnitUUIDs.Add(sdOrgUnit.Uuid);
                                            logger.LogWarning($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because SD orgunit {changedEmployment.DepartmentUUIDIdentifier} was not mapped to a SOFD orgunit.");
                                            sofdPerson.Affiliations.Remove(sofdAffiliation);
                                            continue;
                                        }
                                    }

                                }
                            }
                            if (!sofdOrgUnits.Any(ou => ou.Uuid == sofdAffiliation.OrgUnitUuid))
                            {
                                missingSDOrgUnitUUIDs.Add(changedEmployment.DepartmentUUIDIdentifier);
                                logger.LogWarning($"Not importing affiliation {sofdAffiliation.PositionName} for person {sofdPerson.Uuid} because orgunit {sofdAffiliation.OrgUnitUuid} was not found in SOFD.");
                                if (!isNewAffilition)
                                {
                                    sofdPerson.Affiliations.Remove(sofdAffiliation);
                                }
                            }
                            else
                            {
                                if (isNewAffilition)
                                {
                                    // never create a new affiliation with unknown startdate
                                    if (sofdAffiliation.StartDate == null)
                                    {
                                        sofdAffiliation.StartDate = DateTime.Now;
                                    }
                                    sofdPerson.Affiliations.Add(sofdAffiliation);
                                }
                            }
                            if (changedEmployment.IsDeleted())
                            {
                                logger.LogInformation($"Deleting affiliation {sofdAffiliation.PositionName}, {sofdAffiliation.EmployeeId} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)}");
                                sofdPerson.Affiliations.Remove(sofdAffiliation);
                                continue;
                            }
                        }
                        if (!isExistingSOFDPerson)
                        {
                            sofdService.CreatePerson(sofdPerson);
                        }
                        else if (sofdPerson.IsChanged())
                        {
                            sofdService.UpdatePerson(sofdPerson);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.LogWarning(e, $"Failed to sync person with Cpr {Helper.FormatCprForLog(changedPerson.PersonCivilRegistrationIdentifier)}");
                        databaseContext.Add(new FailedSyncPerson(changedPerson.PersonCivilRegistrationIdentifier, e.Message));
                        databaseContext.SaveChanges();
                    }
                }
                NotifyAboutMissingOrgs(sdOrgUnits, missingSDOrgUnitUUIDs);
            }
            catch (Exception e)
            {
                logger.LogWarning(e, $"Could not get employments from SD {institution.Identifier} cpr: {Helper.FormatCprForLog(cpr)}");
            }
        }

        private void NotifyAboutMissingOrgs(List<SDOrgUnit> sdOrgUnits, HashSet<string> missingSDOrgUnitUUIDs)
        {
            logger.LogInformation($"Found {missingSDOrgUnitUUIDs.Count} SD orgunits with missing tags");
            var notifications = new List<Notification>();
            foreach (var uuid in missingSDOrgUnitUUIDs) {
                var sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == uuid).FirstOrDefault();
                if (sdOrgUnit == null)
                {
                    logger.LogDebug($"Could not find SD OrgUnit with UUID {uuid}");
                }
                else
                {
                    var notification = new Notification();
                    notification.AffectedEntityUuid = sdOrgUnit.Uuid;
                    notification.AffectedEntityName = sdOrgUnit.Name;
                    notification.EventDate = DateTime.Now.Date;
                    notification.Message = $"Der er ansatte i SD enheden {sdOrgUnit.Name} ({sdOrgUnit.DepartmentIdentifier}), men der findes ikke et matchende SD-tag, så ansatte i denne enhed kommer ikke over i OS2sofd";
                    notifications.Add(notification);
                }
                try
                {
                    sofdService.Notify(notifications);
                }
                catch (Exception e)
                {
                    logger.LogWarning(e, $"Failed to crate notifications in sofd");
                }
            }
        }
    }
}