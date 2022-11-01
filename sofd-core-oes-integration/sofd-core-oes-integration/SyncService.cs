using DigitalIdentity;
using DigitalIdentity.Email;
using DigitalIdentity.S3;
using DigitalIdentity.SOFD;
using DigitalIdentity.SOFD.Model;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using sofd_core_oes_integration.Database;
using sofd_core_oes_integration.Database.Model;
using sofd_core_oes_integration.OES;
using sofd_core_oes_integration.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace sofd_core_oes_integration
{
    public class SyncService : BaseClass<SyncService>
    {
        private readonly SOFDService sofdService;
        private readonly DatabaseContext databaseContext;
        private readonly OESService oesService;
        private readonly EmailService emailService;
        private readonly S3Service s3Service;

        public SyncService(IServiceProvider sp) : base(sp)
        {
            sofdService = sp.GetService<SOFDService>();
            databaseContext = sp.GetService<DatabaseContext>();
            oesService = sp.GetService<OESService>();
            emailService = sp.GetService<EmailService>();
            s3Service = sp.GetService<S3Service>();
        }

        public void Synchronize()
        {
            try
            {
                logger.LogInformation("SyncService executing");
                InitializeDatabase();
                SynchronizeOrgUnitsToDB();                
                logger.LogInformation("SyncService finsihed");
            }
            catch (Exception e)
            {
                logger.LogError(e, "Unhandled exception");
            }
        }

        private void InitializeDatabase()
        {
            databaseContext.Database.Migrate();
        }

        private void SynchronizeOrgUnitsToDB()
        {
            // Get all ØS-tagged org units from SOFD
            var sofdOrgUnits = sofdService.GetOrgUnits();

            // Collect validation errors
            var allValidationErrors = new List<string>();
            sofdOrgUnits.ForEach(o => allValidationErrors.AddRange(o.ValidationErrors));
            
            // log all validation errors as warnings
            allValidationErrors.ForEach(e => logger.LogWarning(e));

            // stop integration if there are any validation error
            if (allValidationErrors.Count > 0)
            {
                // Send a mail with all validation errors
                emailService.SendValidationErrors(allValidationErrors);
                logger.LogWarning($"Found {allValidationErrors.Count} validation errors. Stopping sync.");
                return;
            }

            // Update database with data from SOFD
            var updatedDatabaseIds = new List<int>();
            foreach (var rootOrg in sofdOrgUnits.Where(o => o.IsRoot) )
            {
                GenerateDBOrgUnitsRecursive(rootOrg, ref updatedDatabaseIds);
            }
            
            // Update database records that are not found in SOFD
            var thisYear = DateTime.Now.Year.ToString();
            var toBeInactivated = databaseContext.OrgUnits
                .Where(o => !updatedDatabaseIds.Contains(o.Id) && (o.SBENAAR == null || o.SBENAAR == thisYear))
                .ToList();
            InactivateDBOrgUnits(toBeInactivated);

            // Generate files to ØS
            oesService.CheckFailSafe();
            using var admOrgs = oesService.GenerateAdmOrgExcel();
            using var institutions = oesService.GenerateInstitutionExcel();

            // upload files to S3
            var date = DateTime.Now.ToString("yyyy-MM-dd");
            s3Service.UploadFile(appSettings.S3Settings.BucketPath, $"{date}_Admorg.xlsx", admOrgs);
            s3Service.UploadFile(appSettings.S3Settings.BucketPath, $"{date}_Institutioner.xlsx", institutions);
        }

        private void InactivateDBOrgUnits(List<DBOrgUnit> dbOrgUnits)
        {
            var suffix = " - LUKKET";
            foreach (var dbUnit in dbOrgUnits)
            {
                var name = dbUnit.TXTLIN.Replace(suffix, "");
                dbUnit.TXTLIN = name.Truncate(256-suffix.Length) + suffix;
                dbUnit.TEKST1 = name.Truncate(35 - suffix.Length) + suffix;
                dbUnit.SBENAAR = DateTime.Now.Year.ToString();
            }
            databaseContext.SaveChanges();
        }

        private void GenerateDBOrgUnitsRecursive(OrgUnit node, ref List<int> updatedDatabaseIds)
        {
            if (node.IsValid)
            {
                var thisYear = DateTime.Now.Year.ToString();
                var dbUnit = databaseContext.OrgUnits
                    .Where(o => o.TXTSTRUK == node.OESTagValue && (o.SBENAAR == null || o.SBENAAR == thisYear))
                    .SingleOrDefault();
                if (dbUnit == null)
                {
                    dbUnit = new DBOrgUnit();                    
                    dbUnit.KTOPDEL = "O";
                    dbUnit.FBENAAR = thisYear;
                    dbUnit.TXTSTRUK = node.OESTagValue;
                    // add trailing zeros to sequencenumber to up to a length of 10 characaters
                    var oesTag = node.OESTagValue.Replace("-", "");
                    dbUnit.INSTNR = oesTag + new String('0', 10 - oesTag.Length);
                    databaseContext.Add(dbUnit);
                }
                dbUnit.SOFDUuid = node.Uuid;
                dbUnit.TXTLIN = node.Name.Truncate(256);
                dbUnit.CVR = node.Cvr?.ToString();
                dbUnit.TEKST1 = node.Name.Truncate(35);
                dbUnit.TEKST2 = "";
                dbUnit.ADR1 = node.PostAddresses.FirstOrDefault()?.Street?.Truncate(35);
                dbUnit.ADR2 = "";
                dbUnit.PNR = node.PostAddresses.FirstOrDefault()?.PostalCode.Truncate(4);
                dbUnit.TLF = node.Phones.Where(p => p.Prime).FirstOrDefault()?.PhoneNumber?.Truncate(8);
                dbUnit.INSTEMAIL = "";
                dbUnit.SBENAAR = null;

                databaseContext.SaveChanges();
                updatedDatabaseIds.Add(dbUnit.Id);
                foreach (var child in node.TaggedChildren)
                {
                    child.Cvr = child.Cvr != null ? child.Cvr : node.Cvr;
                    GenerateDBOrgUnitsRecursive(child, ref updatedDatabaseIds);
                }
            }
        }
    }
}