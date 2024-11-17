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

        // specifies how far into the future we want to check for changes
        private readonly DateTime lookAhead = DateTime.Now.AddMonths(3);

        public PersonSyncService(IServiceProvider sp) : base(sp)
        {
            sdService = sp.GetService<SDService>();
            sofdService = sp.GetService<SOFDService>();
            databaseContext = sp.GetService<DatabaseContext>();
        }

        public void Synchronize(Dictionary<string, string> sdManagerMap)
        {
            try
            {
                if (!appSettings.EmployeeSyncEnabled)
                {
                    logger.LogInformation("PersonSyncService not enabled");
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
                //if (appSettings.FunctionSyncEnabled)
                //{
                //    SynchronizeFunctionsToSOFD();
                //}
                if (appSettings.SynchronizeManagerFromSDEnabled)
                {
                    SynchronizeManagerFromSD(sdManagerMap);
                }
                logger.LogInformation("PersonSyncService finsihed");
            }
            catch (Exception e)
            {
                logger.LogError(e, "Unhandled exception");
            }
        }

        //private void SynchronizeFunctionsToSOFD()
        //{
        //    var sdOrgFunctions = sdService.GetOrgFunctions();
        //    sofdPersons = sofdService.GetPersons();
        //    var dbOrgUnits = databaseContext.DBOrgUnits.ToArray();
        //    var managerMap = new Dictionary<string, Manager>();
        //    foreach (var sdOrgFunction in sdOrgFunctions)
        //    {
        //        var sofdUuid = dbOrgUnits.Where(o => o.VirtualUuid == sdOrgFunction.uuid).Select(o => o.SofdUuid).SingleOrDefault();
        //        if (sofdUuid == null)
        //        {
        //            continue;
        //        }
        //        var managerPerson = sdOrgFunction.orgFunktioner.Where(of => of.klasseNavn == "SD - Leder" && of.start <= DateTime.Now.Date && of.slut >= DateTime.Now.Date).FirstOrDefault()?.personer.FirstOrDefault();
        //        if (managerPerson != null)
        //        {
        //            var sofdPerson = sofdPersons.Where(p => p.Cpr == managerPerson.brugervendtNoegle).FirstOrDefault();
        //            if (sofdPerson == null)
        //            {
        //                logger.LogWarning($"Manager {managerPerson.navn} ({Helper.FormatCprForLog(managerPerson.brugervendtNoegle)}) for {sdOrgFunction.navn} could not be found in SOFD");
        //                continue;
        //            }
        //            var manager = new Manager();
        //            manager.Name = sofdPerson.GetName();
        //            manager.Uuid = sofdPerson.Uuid;
        //            manager.Inherited = false;
        //            managerMap.Add(sofdUuid, manager);
        //        }
        //    }
        //    var sofdRoot = sofdOrgUnits.Where(o => o.ParentUuid == null).Single();
        //    AddManagerRecursive(sofdRoot, managerMap, null);
        //}

        private void SynchronizeManagerFromSD(Dictionary<string, string> sdManagerMap)
        {
            sofdPersons = sofdService.GetPersons();
            var managers = new List<OrgUnitManagerDto>();
            foreach (var sdOrgUnitUuid in sdManagerMap.Keys)
            {
                if (!sofdOrgUnits.Any(o => o.Uuid == sdOrgUnitUuid))
                {
                    logger.LogInformation($"Not adding manager for sd orgunit uuid {sdOrgUnitUuid} because no corresponding sofd orgunit was found");
                    continue;
                }
                var sofdPerson = sofdPersons.Where(p => p.Affiliations.Any(a => a.isActive() && a.EmployeeId == sdManagerMap[sdOrgUnitUuid])).FirstOrDefault();
                if (sofdPerson == null)
                {
                    logger.LogInformation($"Not adding manager for sd orgunit uuid {sdOrgUnitUuid} because no active affiliation was found with employeeId {sdManagerMap[sdOrgUnitUuid]}");
                }
                else
                {
                    var newManager = new OrgUnitManagerDto();
                    newManager.orgunitUuid = sdOrgUnitUuid;
                    newManager.managerUuid = sofdPerson.Uuid;
                    if (managers.Any(m => m.orgunitUuid == newManager.orgunitUuid) )
                    {
                        logger.LogInformation($"Not adding {newManager.managerUuid} as manager because orgunit with uuid {sdOrgUnitUuid} already has a manager");
                    }
                    else
                    {
                        managers.Add(newManager);
                    }
                }
            }
            sofdService.UpdateManagers(managers);
        }

        private void SynchronizePersonsToSOFD()
        {
            // handle manually added fullsync persons
            var fullSyncPersons = databaseContext.FullSyncPersons;
            var now = DateTime.Now;
            if (fullSyncPersons.Count() > 0)
            {
                sofdPersons = sofdService.GetPersons();
                var sdOrgUnitDict = new Dictionary<string, List<SDOrgUnit>>();
                var sdProfessionDict = new Dictionary<string, Dictionary<string, SDProfession>>();
                foreach (var person in databaseContext.FullSyncPersons)
                {
                    foreach (var institution in appSettings.SDSettings.Institutions)
                    {
                        List<SDOrgUnit> sdOrgUnits = null;
                        Dictionary<string, SDProfession> sdProfessions = null;
                        if (sdOrgUnitDict.ContainsKey(institution.Identifier))
                        {
                            sdOrgUnits = sdOrgUnitDict[institution.Identifier];
                        }
                        else {
                            sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                            sdOrgUnitDict.Add(institution.Identifier, sdOrgUnits);
                        }
                        if (sdProfessionDict.ContainsKey(institution.Identifier))
                        {
                            sdProfessions = sdProfessionDict[institution.Identifier];
                        }
                        else
                        {
                            sdProfessions = sdService.GetProfessions(institution.Identifier);
                            sdProfessionDict.Add(institution.Identifier, sdProfessions);
                        }                                                
                        logger.LogInformation($"Performing Manual Full Sync For cpr {Helper.FormatCprForLog(person.Cpr)} in institution {institution.Identifier}");
                        FullSynchronizeForInstitution(institution, sdOrgUnits, sdProfessions, person.Cpr);
                        DeltaSynchronizeForInstitution(institution, sdOrgUnits, sdProfessions, now, lookAhead, person.Cpr);
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
                syncInfo = new SynchronizeInfo { PersonsLastSync = now };
                databaseContext.Add(syncInfo);
                foreach (var institution in appSettings.SDSettings.Institutions)
                {
                    List<SDOrgUnit> sdOrgUnits = null;
                    sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                    var sdProfessions = sdService.GetProfessions(institution.Identifier);
                    fullSDPersons.AddRange(sdService.GetPersons(institution.Identifier));
                    logger.LogInformation($"Performing Full Sync for institution {institution}");
                    FullSynchronizeForInstitution(institution,sdOrgUnits, sdProfessions);
                    DeltaSynchronizeForInstitution(institution, sdOrgUnits, sdProfessions, now, lookAhead);
                }
            }
            else
            {
                sofdPersons = sofdService.GetPersons();
                var changedFrom = syncInfo.PersonsLastSync;

                foreach (var institution in appSettings.SDSettings.Institutions)
                {
                    List<SDOrgUnit> sdOrgUnits = null;
                    sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                    var sdProfessions = sdService.GetProfessions(institution.Identifier);
                    logger.LogInformation($"Performing Delta Sync for institution {institution.Identifier}");
                    DeltaSynchronizeForInstitution(institution,sdOrgUnits, sdProfessions, changedFrom.AddMonths(-2), lookAhead);
                }
                syncInfo.PersonsLastSync = now;
            }
            databaseContext.SaveChanges();
        }

        private void FullSynchronizeForInstitution(Institution institution, List<SDOrgUnit> sdOrgUnits, Dictionary<string, SDProfession> sdProfessions, string cpr = null)
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
                            if (appSettings.PrivateAddressEnabled && sdPerson.HasValidAddress())
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

                            // delete this employment from any other sofd person (SD can delete an employment id and reuse it on other persons /cry)
                            var badSOFDPersons = sofdPersons.Where(p => p.Uuid != sofdPerson.Uuid && p.Affiliations.Any(e => e.Master == master && e.MasterId == employmentMasterId));
                            foreach (var badSOFDPerson in badSOFDPersons)
                            {
                                logger.LogWarning($"Removing affiliation with masterId {employmentMasterId} from person with uuid {badSOFDPerson.Uuid} because it was used on another person");
                                badSOFDPerson.Affiliations.RemoveAll(e => e.Master == master && e.MasterId == employmentMasterId);
                                sofdService.UpdatePerson(badSOFDPerson);
                            }

                            // continue normal sync logic
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

                            if (!String.IsNullOrEmpty(sdEmployment.EmploymentName)) // first check if it has an EmploymentName
                            {
                                sofdAffiliation.PositionName = sdEmployment.EmploymentName;
                            }
                            else if (appSettings.JobPositionMap.ContainsKey(sdEmployment.JobPositionIdentifier)) // check if we have a mapping for this positionid
                            {
                                sofdAffiliation.PositionName = appSettings.JobPositionMap[sdEmployment.JobPositionIdentifier];
                            }
                            else if (String.IsNullOrEmpty(sofdAffiliation.PositionName)) // set some fallbacks if affiliation has no value yet
                            {
                                if (sdProfessions.ContainsKey(sdEmployment.JobPositionIdentifier)) // fallback to position name
                                {
                                    sofdAffiliation.PositionName = sdProfessions[sdEmployment.JobPositionIdentifier].Name;
                                }
                                else
                                {
                                    sofdAffiliation.PositionName = "Ukendt";
                                }
                            }
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
                            var sdNumerator = Math.Round(occupationHours * (sdEmployment.OccupationRate ?? 1), 2);
                            if (sofdAffiliation.WorkingHoursNumerator != sdNumerator)
                            {
                                sofdAffiliation.WorkingHoursNumerator = sdNumerator;
                            }
                            if (sofdAffiliation.WorkingHoursDenominator != occupationHours)
                            {
                                sofdAffiliation.WorkingHoursDenominator = occupationHours;
                            }
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
                                // setting orgunit uuid to the same af department uuid or to a parent orgunit uuid if not found
                                if (institution.Prime)
                                {
                                    var currentUuid = sdEmployment.DepartmentUUIDIdentifier;
                                    while (currentUuid != null)
                                    {
                                        var sofdOrgUnit = sofdOrgUnits.Where(o => o.Uuid == currentUuid && !o.Deleted).FirstOrDefault();
                                        if (sofdOrgUnit != null)
                                        {
                                            sofdAffiliation.OrgUnitUuid = sofdOrgUnit.Uuid;
                                            break;
                                        }
                                        else
                                        {
                                            // find uuid of parent
                                            currentUuid = sdOrgUnits.Where(o => o.Uuid == currentUuid).FirstOrDefault()?.ParentUuid;
                                        }
                                    }
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
                                        if( sdOrgUnit != null )
                                        {
                                            missingSDOrgUnitUUIDs.Add(sdOrgUnit.Uuid);
                                        }
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
                    var removedCount = sofdPerson.Affiliations.RemoveAll(a => a.Master == master);
                    if (removedCount > 0)
                    {
                        sofdService.UpdatePerson(sofdPerson);
                    }                    
                }
                NotifyAboutMissingOrgs(sdOrgUnits, missingSDOrgUnitUUIDs);
            }
            catch (Exception e)
            {
                logger.LogWarning(e, $"Could not get employments from SD {institution.Identifier} cpr: {Helper.FormatCprForLog(cpr)}");
            }
        }

        private void DeltaSynchronizeForInstitution(Institution institution, List<SDOrgUnit> sdOrgUnits, Dictionary<string, SDProfession> sdProfessions, DateTime changedFrom, DateTime changedTo, string cpr = null)
        {
            try
            {
                var master = appSettings.SOFDSettings.MasterPrefix + institution.Identifier;

                var changedPersons = sdService.GetChanges(institution.Identifier, changedFrom, changedTo, cpr);
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
                            if (appSettings.PrivateAddressEnabled && sdPerson.HasValidAddress())
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
                        foreach (var changedEmployment in changedPerson.ChangedEmployments)
                        {
                            logger.LogTrace($"EmploymentIdentifier: {changedEmployment.EmploymentIdentifier}");
                            // if instititution is not the prime institution, we prefix the employmentidentifier to prevent duplicates
                            var employmentMasterId = institution.Prime ? changedEmployment.EmploymentIdentifier : institution.Identifier + "-" + changedEmployment.EmploymentIdentifier;

                            // delete this employment from any other sofd person (SD can delete an employment id and reuse it on other persons /cry)
                            var badSOFDPersons = sofdPersons.Where(p => p.Uuid != sofdPerson.Uuid && p.Affiliations != null && p.Affiliations.Any(e => e.Master == master && e.MasterId == employmentMasterId));
                            foreach (var badSOFDPerson in badSOFDPersons)
                            {
                                logger.LogWarning($"Removing affiliation with masterId {employmentMasterId} from person with uuid {badSOFDPerson.Uuid} because it was used on another person");
                                badSOFDPerson.Affiliations.RemoveAll(e => e.Master == master && e.MasterId == employmentMasterId);
                                sofdService.UpdatePerson(badSOFDPerson);
                            }

                            // continue normal sync logic
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
                            var changedSDEmployment = SDEmployment.FromEmploymentTypeChanged(changedEmployment, isNewAffilition);

                            sofdPerson.AnniversaryDate = changedSDEmployment.AnniversaryDate ?? sofdPerson.AnniversaryDate;
                            // set first employment as lowest employmentdate
                            if (changedSDEmployment.StartDate != null && (sofdPerson.FirstEmploymentDate == null || sofdPerson.FirstEmploymentDate > changedSDEmployment.StartDate))
                            {
                                sofdPerson.FirstEmploymentDate = changedSDEmployment.StartDate;
                            }
                            sofdAffiliation.PositionId = changedSDEmployment.JobPositionIdentifier ?? sofdAffiliation.PositionId;
                            if (changedSDEmployment.JobPositionIdentifier != null || isNewAffilition)
                            {
                                if (!String.IsNullOrEmpty(changedSDEmployment.EmploymentName)) // first check if it has an EmploymentName
                                {
                                    sofdAffiliation.PositionName = changedSDEmployment.EmploymentName;
                                }
                                else if (changedSDEmployment.JobPositionIdentifier != null && appSettings.JobPositionMap.ContainsKey(changedSDEmployment.JobPositionIdentifier)) // check if we have a mapping for this positionid
                                {
                                    sofdAffiliation.PositionName = appSettings.JobPositionMap[changedSDEmployment.JobPositionIdentifier];
                                }
                                else if (String.IsNullOrEmpty(sofdAffiliation.PositionName)) // set some fallbacks if affiliation has no value yet
                                { 
                                    if (changedSDEmployment.JobPositionIdentifier != null && sdProfessions.ContainsKey(changedSDEmployment.JobPositionIdentifier)) // fallback to position name
                                    {
                                        sofdAffiliation.PositionName = sdProfessions[changedSDEmployment.JobPositionIdentifier].Name;
                                    }
                                    else
                                    {
                                        sofdAffiliation.PositionName = "Ukendt";
                                    }
                                }
                            }
                            sofdAffiliation.EmploymentTerms = changedSDEmployment.GetOPUSEmploymentTermsId() ?? sofdAffiliation.EmploymentTerms;
                            sofdAffiliation.EmploymentTermsText = changedSDEmployment.GetOPUSEmploymentTermsText() ?? sofdAffiliation.EmploymentTermsText;
                            sofdAffiliation.PayGrade = changedSDEmployment.SalaryClassIdentifier ?? sofdAffiliation.PayGrade;
                            sofdAffiliation.StartDate = changedSDEmployment.StartDate ?? sofdAffiliation.StartDate;
                            if (changedSDEmployment.StopDate.Changed)
                            {
                                sofdAffiliation.StopDate = changedSDEmployment.StopDate.Value;
                            }
                            else if (sofdAffiliation.StopDate?.Date > DateTime.Now.Date ) {
                                sofdAffiliation.StopDate = null; // if we have a registered future stop-date in sofd, but can't see it in the foreseeable future (3 months), then delete it.
                            }

                            if (changedSDEmployment.OccupationRate != null)
                            {
                                var occupationHours = appSettings.DefaultOccupationHours;
                                if (sofdAffiliation.PositionId != null && appSettings.PositionOccupationHoursMap.ContainsKey(sofdAffiliation.PositionId))
                                {
                                    occupationHours = appSettings.PositionOccupationHoursMap[sofdAffiliation.PositionId];
                                }
                                var sdNumerator = Math.Round(occupationHours * (changedSDEmployment.OccupationRate ?? 1), 2);
                                if (sofdAffiliation.WorkingHoursNumerator != sdNumerator)
                                {
                                    sofdAffiliation.WorkingHoursNumerator = sdNumerator;
                                }
                                if (sofdAffiliation.WorkingHoursDenominator != occupationHours)
                                {
                                    sofdAffiliation.WorkingHoursDenominator = occupationHours;
                                }
                            }
                            if (changedSDEmployment.DepartmentUUIDIdentifier != null)
                            {
                                if (appSettings.EmployeeSyncUseTags)
                                {
                                    // setting orgunit uuid based on Tags in SOFD core
                                    var sdOrgunit = sdOrgUnits.Where(o => o.Uuid == changedSDEmployment.DepartmentUUIDIdentifier).First();
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
                                    // setting orgunit uuid to the same af department uuid or to a parent orgunit uuid if not found
                                    if (institution.Prime)
                                    {
                                        var currentUuid = changedSDEmployment.DepartmentUUIDIdentifier;
                                        while (currentUuid != null)
                                        {
                                            var sofdOrgUnit = sofdOrgUnits.Where(o => o.Uuid == currentUuid && !o.Deleted).FirstOrDefault();
                                            if (sofdOrgUnit != null)
                                            {
                                                sofdAffiliation.OrgUnitUuid = sofdOrgUnit.Uuid;
                                                break;
                                            }
                                            else
                                            {
                                                // find uuid of parent
                                                currentUuid = sdOrgUnits.Where(o => o.Uuid == currentUuid).FirstOrDefault()?.ParentUuid;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // if this is not the prime sd institution then apply org mappings
                                        string mappedUuid = null;
                                        var sdOrgUnit = sdOrgUnits.Where(o => o.Uuid == changedSDEmployment.DepartmentUUIDIdentifier).FirstOrDefault();
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
                                            if( sdOrgUnit != null )
                                            {
                                                missingSDOrgUnitUUIDs.Add(sdOrgUnit.Uuid);
                                            }
                                            logger.LogWarning($"Not importing affiliation {sofdAffiliation.PositionName} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because SD orgunit {changedSDEmployment.DepartmentUUIDIdentifier} was not mapped to a SOFD orgunit.");
                                            sofdPerson.Affiliations.Remove(sofdAffiliation);
                                            continue;
                                        }
                                    }

                                }
                            }
                            if (sofdAffiliation.OrgUnitUuid == null) {
                                logger.LogWarning($"Not importing affiliation {sofdAffiliation.PositionName} for person {sofdPerson.Uuid} because orgunit info could not be resolved");
                                sofdPerson.Affiliations.Remove(sofdAffiliation);
                                continue;
                            }
                            if (!sofdOrgUnits.Any(ou => ou.Uuid == sofdAffiliation.OrgUnitUuid))
                            {
                                missingSDOrgUnitUUIDs.Add(changedSDEmployment.DepartmentUUIDIdentifier);
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
                            if (changedSDEmployment.IsDeleted())
                            {
                                logger.LogInformation($"Deleting affiliation {sofdAffiliation.PositionName}, {sofdAffiliation.EmployeeId} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because it is deleted in SD");
                                sofdPerson.Affiliations.Remove(sofdAffiliation);
                                continue;
                            }
                            if (sofdAffiliation.StartDate >= sofdAffiliation.StopDate) {
                                logger.LogInformation($"Removing affiliation {sofdAffiliation.PositionName}, {sofdAffiliation.EmployeeId} for person with Uuid {sofdPerson.Uuid} and Cpr {Helper.FormatCprForLog(sofdPerson.Cpr)} because StartDate >= StopDate");
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