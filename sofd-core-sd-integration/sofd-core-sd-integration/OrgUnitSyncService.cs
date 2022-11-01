using DigitalIdentity;
using DigitalIdentity.SDMOX;
using DigitalIdentity.SDMOX.Model;
using DigitalIdentity.SOFD;
using DigitalIdentity.SOFD.Model;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using sofd_core_sd_integration.Database;
using sofd_core_sd_integration.Database.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace sofd_core_sd_integration
{
    public class OrgUnitSyncService : BaseClass<OrgUnitSyncService>
    {
        private readonly SOFDService sofdService;
        private readonly DatabaseContext databaseContext;
        private readonly SDMOXService sdMOXService;
        private static readonly string SDTag = "SD";

        public OrgUnitSyncService(IServiceProvider sp) : base(sp)
        {
            sdMOXService = sp.GetService<SDMOXService>();
            sofdService = sp.GetService<SOFDService>();
            databaseContext = sp.GetService<DatabaseContext>();
        }

        public void Synchronize()
        {
            try
            {
                if (!appSettings.OrgSyncEnabled)
                {
                    logger.LogWarning("OrgUnitSyncService not enabled");
                    return;
                }
                logger.LogInformation("OrgUnitSyncService executing");
                // manually create missing orgs
                //SDMoxOrgDto department;
                //department = new SDMoxOrgDto() { Uuid = "c26e4cbb-f2bb-4084-9762-568f714a16ad", ParentUuid = "ebff2d2f-1971-4f78-88b3-54ce5e441ed6", Level = "Afdelings-niveau", Name = "Pindstrupskolen, SFO og Bornehus", };
                //sdMOXService.Import(department);

                SynchronizeOrgUnitsToSD();
                logger.LogInformation("OrgUnitSyncService finsihed");
            }
            catch (Exception e)
            {
                logger.LogError(e, "Unhandled exception");
            }
        }

        private void SynchronizeOrgUnitsToSD()
        {
            var sofdOrgUnits = sofdService.GetOrgUnits();            

            var sofdRoot = sofdOrgUnits.Where(o => o.ParentUuid == null).Single();
            ProcessSDTags(sofdRoot, sofdOrgUnits);
            var taggedOrgUnits = sofdOrgUnits.Where(o => o.IsSDTagged).OrderByDescending(o => o.Level).ToList();
            
            // Handle Inserts and Updates
            foreach ( var taggedOrgUnit in taggedOrgUnits)
            {
                var dbOrgUnit = databaseContext.DBOrgUnits.Where(o => o.SofdUuid == taggedOrgUnit.Uuid).FirstOrDefault();
                var dbOrgUnitParent = taggedOrgUnit.ParentSDUnit != null ? databaseContext.DBOrgUnits.Where(o => o.SofdUuid == taggedOrgUnit.ParentSDUnit.Uuid).FirstOrDefault() : null;
                bool isNew = false;
                if (dbOrgUnit == null)
                {
                    dbOrgUnit = new DBOrgUnit();
                    dbOrgUnit.SofdUuid = taggedOrgUnit.Uuid;
                    // if tag is a "NY" value - we create a new uuid for a virtual NY-unit in SD.
                    dbOrgUnit.VirtualUuid = taggedOrgUnit.IsNYUnit ? Guid.NewGuid().ToString() : null;
                    dbOrgUnit.Created = DateTime.Now;
                    isNew = true;
                    databaseContext.Add(dbOrgUnit);                    
                }
                dbOrgUnit.TrackChanges();
                dbOrgUnit.Deleted = null;
                bool isMoved = dbOrgUnit.ParentVirtualUuid != dbOrgUnitParent?.VirtualUuid;
                dbOrgUnit.ParentVirtualUuid = dbOrgUnitParent?.VirtualUuid;

                dbOrgUnit.Name = taggedOrgUnit.Name;
                dbOrgUnit.Level = taggedOrgUnit.Level;

                dbOrgUnit.PNumber = taggedOrgUnit.Pnr?.ToString();
                dbOrgUnit.Street = taggedOrgUnit.PostAddresses.FirstOrDefault()?.Street;
                dbOrgUnit.PostalCode = taggedOrgUnit.PostAddresses.FirstOrDefault()?.PostalCode;
                dbOrgUnit.City = taggedOrgUnit.PostAddresses.FirstOrDefault()?.City;
                dbOrgUnit.Phone = taggedOrgUnit.Phones.Where(p => p.Prime).FirstOrDefault()?.PhoneNumber;

                // org is the NY-enhed virtual unit
                var org = new SDMoxOrgDto()
                {
                    Uuid = dbOrgUnit.VirtualUuid,
                    ParentUuid = dbOrgUnit.ParentVirtualUuid,
                    Level = $"NY{dbOrgUnit.Level}-niveau",
                    Name = dbOrgUnit.Name,
                    Pnr = dbOrgUnit.PNumber,
                    Address = dbOrgUnit.Street,
                    PostalCode = dbOrgUnit.PostalCode,
                    City = dbOrgUnit.City,
                    Phone = dbOrgUnit.Phone
                };
                // department is the "afdelings-niveau" unit
                var department = org.GetDepartmentCopy(dbOrgUnit.SofdUuid, taggedOrgUnit.IsNYUnit ? dbOrgUnit.VirtualUuid : dbOrgUnit.ParentVirtualUuid);

                if (isNew)
                {
                    if (taggedOrgUnit.IsNYUnit)
                    {
                        sdMOXService.Import(org);
                    }
                    sdMOXService.Import(department);
                }
                else
                {
                    if (isMoved)
                    {
                        dbOrgUnit.Changed = DateTime.Now;
                        // if the tagged orgunit is a NY unit, we move the NY-unit in SD
                        // otherwise me move the department
                        if (taggedOrgUnit.IsNYUnit)
                        {
                            sdMOXService.Flyt(org);
                        }
                        else
                        {
                            sdMOXService.Flyt(department);
                        }                        
                    }
                    if (dbOrgUnit.IsChanged())
                    {
                        dbOrgUnit.Changed = DateTime.Now;
                        if (taggedOrgUnit.IsNYUnit)
                        {
                            sdMOXService.Ret(org);
                        }                        
                        sdMOXService.Ret(department);
                    }
                }
                databaseContext.SaveChanges();
            }

            // Handle Deletions
            var existingOrgUuids = taggedOrgUnits.Select(o => o.Uuid).ToList();
            var toBeDeleted = databaseContext.DBOrgUnits.Where(o => o.Deleted == null && !existingOrgUuids.Contains(o.SofdUuid)).ToList();
            foreach (var dbOrgUnit in toBeDeleted)
            {
                dbOrgUnit.Changed = DateTime.Now;
                dbOrgUnit.Deleted = DateTime.Now;
                dbOrgUnit.ParentVirtualUuid = appSettings.SDMOXSettings.DeletedOrgsUuid;
                // org is the NY-enhed virtual unit
                var org = new SDMoxOrgDto()
                {
                    // if it's a ny-enhed (with virtual uuid) we just move the virtual unit
                    // otherwise we move the actual (nuværende) unit
                    Uuid = dbOrgUnit.VirtualUuid ?? dbOrgUnit.SofdUuid,
                    ParentUuid = dbOrgUnit.ParentVirtualUuid
                };
                sdMOXService.Flyt(org);
                databaseContext.SaveChanges();
            }
        }

        private void ProcessSDTags(OrgUnit node, List<OrgUnit> sofdOrgUnits)
        {
            var nodeUuid = node.Uuid;
            var children = sofdOrgUnits.Where(o => o.ParentUuid == nodeUuid);
            var sdTag = node.Tags.FirstOrDefault(t => t.Tag == SDTag);            
            if (sdTag != null)
            {
                if (string.IsNullOrEmpty(sdTag.CustomValue))
                {
                    throw new Exception($"Missing Tag value for unit {node.Name} ({node.Uuid})");
                }
                node.IsSDTagged = true;
                node.IsNYUnit = sdTag.CustomValue.StartsWith("NY");
                node.Level = node.IsNYUnit ? int.Parse(Regex.Replace(sdTag.CustomValue, @"\D", "")) : 0;
            }

            foreach (var child in children)
            {
                child.Parent = node;
                child.ParentSDUnit = node.IsSDTagged ? node : node.ParentSDUnit;
                ProcessSDTags(child, sofdOrgUnits);
            }
        }
    }
}