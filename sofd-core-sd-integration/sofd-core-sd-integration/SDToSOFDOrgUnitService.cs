using DigitalIdentity;
using DigitalIdentity.SD;
using DigitalIdentity.SD.Model;
using DigitalIdentity.SOFD;
using DigitalIdentity.SOFD.Model;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace sofd_core_sd_integration
{
    public class SDToSOFDOrgUnitService : BaseClass<SDToSOFDOrgUnitService>
    {
        private readonly SOFDService sofdService;
        private readonly SDService sdService;
        public SDToSOFDOrgUnitService(IServiceProvider sp) : base(sp)
        {
            sofdService = sp.GetService<SOFDService>();
            sdService = sp.GetService<SDService>();
        }

        public Dictionary<string, string> Synchronize()
        {
            if (!appSettings.SDToSofdOrgSyncEnabled)
            {
                logger.LogInformation("SDToSofdOrgSyncEnabled not enabled");
                return null;
            }
            logger.LogInformation("SDToSofdOrgSyncEnabled executing");
            var sdManagerMap = SynchronizeOrgUnitsToSOFD();
            logger.LogInformation("SDToSofdOrgSyncEnabled finsihed");
            return sdManagerMap;
        }

        private Dictionary<string, string> SynchronizeOrgUnitsToSOFD()
        {
            var sofdOrgUnits = sofdService.GetOrgUnits();
            var sdManagerMap = new Dictionary<string, string>();
            foreach (var institution in appSettings.SDSettings.Institutions)
            {                
                if (institution.Prime)
                {
                    var sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                    SDOrgUnit topNode;
                    // use an SD orgunit as topnode
                    if (institution.TopOrgUuid != null)
                    {
                        topNode = sdOrgUnits.Where(o => o.Uuid == institution.TopOrgUuid).Single();
                    }
                    // use a SOFD orgunit as topnode
                    // this is used in cases where the SD institution have multiple top-nodes
                    else if (institution.SOFDTopOrgUuid != null)
                    {
                        // create a "fake" SD topnode from the SOFDTopOrgUuid
                        var sofdOrgUnit = sofdOrgUnits.Where(o => o.Uuid == institution.SOFDTopOrgUuid).Single();
                        topNode = new SDOrgUnit();
                        topNode.Uuid = institution.SOFDTopOrgUuid;
                        topNode.IsFake = true;
                        // set all the SD top nodes' parent to the "fake" node from SOFD.
                        foreach (var sdTopNode in sdOrgUnits.Where(o => o.ParentUuid == null))
                        {
                            sdTopNode.ParentUuid = topNode.Uuid;
                        }
                        sdOrgUnits.Add(topNode);
                    }
                    else
                    {
                        throw new Exception("Either TopOrgUuid or SOFDTopOrgUuid should be specified");
                    }

                    BuildHierarchyRecursive(sdOrgUnits, topNode);
                    var resultOrgUnits = new List<SDOrgUnit>();
                    BuildResultRecursive(resultOrgUnits, sdOrgUnits, topNode, null);
                    MergeToSOFD(institution, sofdOrgUnits, resultOrgUnits);
                    sdOrgUnits.Where(o => o.IsManagerUnit).ToList().ForEach(o => sdManagerMap.Add(o.Uuid, o.ManagerEmployeeId));                    
                }
                else {
                    // add manager mappings from other institutions
                    if (appSettings.SynchronizeManagerFromSDEnabled) {
                        var sdOrgUnits = sdService.GetOrgUnits(institution.Identifier);
                        foreach (var sdOrgUnit in sdOrgUnits.Where(o => o.IsManagerUnit)) {
                            var mappedSOFDUuid = institution.Mappings.Where(m => m.SDUuid == sdOrgUnit.Uuid).Select(m => m.SOFDUuid).FirstOrDefault();
                            if (mappedSOFDUuid != null && !sdManagerMap.ContainsKey(mappedSOFDUuid)) {
                                sdManagerMap.Add(mappedSOFDUuid, institution.Identifier + "-" + sdOrgUnit.ManagerEmployeeId);
                            }
                        }
                    }                    
                }
            }
            return sdManagerMap;
        }

        private int BuildHierarchyRecursive(List<SDOrgUnit> allOrgunits, SDOrgUnit node)
        {
            if (appSettings.SDSettings.OrgUnitBlacklist.Contains(node.Uuid))
            {
                return 0;
            }
            if (appSettings.SDSettings.OrgUnitExcludeRegex != null && node.Name != null)
            {
                if (Regex.IsMatch(node.Name, appSettings.SDSettings.OrgUnitExcludeRegex, RegexOptions.IgnoreCase))
                {
                    return 0;
                }
            }

            var children = allOrgunits.Where(o => o.ParentUuid == node.Uuid);
            foreach (var child in children)
            {
                node.SubManagerCount += BuildHierarchyRecursive(allOrgunits, child);
            }

            // include node if whitelisted or any nodes with a manager child, og any nodes with more than one sub manager
            var isWhitelisted = appSettings.SDSettings.OrgUnitWhitelist.Contains(node.Uuid);
            node.IncludeUnit = appSettings.SDSettings.AllOrgUnitsIncluded || node.IsFake || isWhitelisted || node.IsManagerUnit || children.Any(c => c.IsManagerUnit) || node.SubManagerCount > 1;
            if (isWhitelisted)
            {
                logger.LogDebug($"Whitelisted orgunit: {node.Name}; Enhedskode: {node.DepartmentIdentifier}; Uuid: {node.Uuid}");
            }

            return (node.IsManagerUnit) ? node.SubManagerCount + 1 : node.SubManagerCount;
        }

        private void BuildResultRecursive(List<SDOrgUnit> resultOrgUnits, List<SDOrgUnit> allOrgunits, SDOrgUnit node, SDOrgUnit parentNode)
        {
            node.NewParentUuid = parentNode?.Uuid;
            if (node.IncludeUnit)
            {
                resultOrgUnits.Add(node);
            }

            var children = allOrgunits.Where(o => o.ParentUuid == node.Uuid);
            foreach (var child in children)
            {
                if (node.IncludeUnit)
                {
                    BuildResultRecursive(resultOrgUnits, allOrgunits, child, node);
                }
                else
                {
                    BuildResultRecursive(resultOrgUnits, allOrgunits, child, parentNode);
                }
            }
        }

        private void MergeToSOFD(SDSettings.Institution institution, List<OrgUnit> sofdOrgUnits, List<SDOrgUnit> sdOrgUnits)
        {
            // handle updates and inserts
            foreach (var sdOrgUnit in sdOrgUnits)
            {
                if (sdOrgUnit.IsFake) {
                    // this is a fake root node from SOFD - don't do any updates on this.
                    continue;
                }
                var master = appSettings.SOFDSettings.MasterPrefix + institution.Identifier;
                var sofdOrgUnit = sofdOrgUnits.Where(so => so.Master == master && so.MasterId == sdOrgUnit.Uuid).SingleOrDefault();
                var isNew = false;
                if (sofdOrgUnit == null)
                {
                    sofdOrgUnit = new OrgUnit();
                    sofdOrgUnit.PostAddresses = new List<PostAddress>();
                    sofdOrgUnit.Phones = new List<Phone>();
                    isNew = true;
                }
                sofdOrgUnit.TrackChanges();
                    
                sofdOrgUnit.Uuid = sdOrgUnit.Uuid;
                sofdOrgUnit.Deleted = false;
                sofdOrgUnit.Master = master;
                sofdOrgUnit.MasterId = sdOrgUnit.Uuid;
                sofdOrgUnit.Name = sdOrgUnit.Name;
                sofdOrgUnit.Shortname = sdOrgUnit.DepartmentIdentifier;
                sofdOrgUnit.ParentUuid = sdOrgUnit.NewParentUuid;

                if (sdOrgUnit.Street != null)
                {
                    var sofdAddress = sofdOrgUnit.PostAddresses.Where(pa => pa.Master == master && pa.MasterId == sdOrgUnit.Uuid).SingleOrDefault();
                    if (sofdAddress == null)
                    {
                        sofdAddress = new PostAddress();
                        sofdAddress.Master = master;
                        sofdAddress.MasterId = sdOrgUnit.Uuid;
                        sofdAddress.AddressProtected = false;
                        sofdAddress.Country = "Danmark";
                        sofdAddress.Prime = !sofdOrgUnit.PostAddresses.Any(pa => pa.Prime);
                        sofdOrgUnit.PostAddresses.Add(sofdAddress);

                    }
                    sofdAddress.Street = sdOrgUnit.Street;
                    sofdAddress.PostalCode = sdOrgUnit.PostalCode;
                    sofdAddress.City = sdOrgUnit.City;
                }

                if (sdOrgUnit.Phone == null)
                {
                    // remove all sd master phones
                    sofdOrgUnit.Phones.RemoveAll(p => p.Master == master);
                }
                else
                { 
                    var phone = sofdOrgUnit.Phones.Where(p => p.Master == master && p.MasterId == sdOrgUnit.Uuid).FirstOrDefault();
                    if (phone == null)
                    {
                        phone = new Phone();
                        phone.Master = master;
                        phone.MasterId = sdOrgUnit.Uuid;
                        phone.PhoneType = "IP";
                        phone.Visibility = "VISIBLE";
                        sofdOrgUnit.Phones.Add(phone);
                    }
                    phone.PhoneNumber = sdOrgUnit.Phone;                    
                }

                if (isNew)
                {
                    sofdService.CreateOrgUnit(sofdOrgUnit);
                }
                else if (sofdOrgUnit.IsChanged())
                {
                    sofdService.UpdateOrgUnit(sofdOrgUnit);   
                }
            }

            // handle deletes
            var toBeDeleted = sofdOrgUnits.Where(so => so.Master == appSettings.SOFDSettings.MasterPrefix + institution.Identifier && !sdOrgUnits.Any(sdo => sdo.Uuid == so.MasterId));
            foreach (var orgUnit in toBeDeleted)
            {
                sofdService.DeleteOrgUnit(orgUnit.Uuid);
            }
            
        }

    }
}