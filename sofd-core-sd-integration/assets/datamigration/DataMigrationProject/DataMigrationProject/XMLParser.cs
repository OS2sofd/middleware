using Microsoft.VisualBasic.FileIO;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Xml.XPath;

namespace DataMigrationProject
{
    public class XMLParser
    {
        public void FixPnr()
        {

            var depDoc = new XPathDocument(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\20201113_ZT_GetDepartment.xml");
            var depNav = depDoc.CreateNavigator();

            var departmentNodes = depNav.Select("//Department");
            var updates = new StringBuilder();
            while (departmentNodes.MoveNext())
            {
                var node = departmentNodes.Current;
                var uuid = node.SelectSingleNode("DepartmentUUIDIdentifier").Value;
                var level = node.SelectSingleNode("DepartmentLevelIdentifier").Value;
                var pnr = node.SelectSingleNode("ProductionUnitIdentifier")?.Value;
                if (level == "Afdelings-niveau" && !String.IsNullOrEmpty(pnr))
                {                    
                    updates.AppendLine($"UPDATE orgunits SET pnr={pnr} WHERE uuid='{uuid}';");
                }
                File.WriteAllText(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\MigrationData\6_update_pnr.sql", updates.ToString());
            }
        }

        public void ParseOrg()
        {
            var excelNameDict = new Dictionary<string, string>();
            using (TextFieldParser parser = new TextFieldParser(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\UUID_NAME_MAP.csv"))
            {
                parser.TextFieldType = FieldType.Delimited;
                parser.SetDelimiters(";");
                while (!parser.EndOfData)
                {
                    string[] fields = parser.ReadFields();
                    excelNameDict.Add(fields[0], fields[1]);
                }
            }

            var orgDoc = new XPathDocument(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\20201113_ZT_GetOrganization.xml");
            var orgNav = orgDoc.CreateNavigator();

            var depDoc = new XPathDocument(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\20201113_ZT_GetDepartment.xml");
            var depNav = depDoc.CreateNavigator();

            var departmentNodes = orgNav.Select("//DepartmentReference");

            var departmentDict = new Dictionary<string, Department>();

            while (departmentNodes.MoveNext())
            {
                var department = new Department();
                var node = departmentNodes.Current;
                department.Identifier = node.SelectSingleNode("DepartmentIdentifier").Value;
                department.UUIDIdentifier = node.SelectSingleNode("DepartmentUUIDIdentifier").Value;
                department.LevelIdentifier = node.SelectSingleNode("DepartmentLevelIdentifier").Value;
                var parent = departmentNodes.Current.SelectSingleNode("DepartmentReference");
                department.ParentUUIDIdentifier = parent?.SelectSingleNode("DepartmentUUIDIdentifier").Value;
                var xpath = $"//Department[DepartmentUUIDIdentifier='{department.UUIDIdentifier}']";
                department.Name = depNav.SelectSingleNode(xpath)?.SelectSingleNode("DepartmentName").Value;
                if (excelNameDict.ContainsKey(department.UUIDIdentifier))
                {
                    department.ExcelName = excelNameDict[department.UUIDIdentifier];
                }
                

                if (!departmentDict.ContainsKey(department.UUIDIdentifier))
                {
                    departmentDict.Add(department.UUIDIdentifier, department);
                }                
            }

            var departments = departmentDict.Values;

            var nuvDeps = departments.Where(d => d.LevelIdentifier == "Afdelings-niveau");
            var nyDeps = departments.Except(nuvDeps);


            // are there any NY deps that doesn't have a corresponding nuv. afdeling?
            var nuvLos = nuvDeps.Select(d => d.ExcelName).ToList();
            var missingNuvs = nyDeps.Where(nd => !nuvLos.Contains(nd.ExcelName));
            var missingNuvString = new StringBuilder();
            if (missingNuvs.Any())
            {
                foreach (var missing in missingNuvs)
                {
                    missingNuvString.AppendLine($"{Guid.NewGuid().ToString()};{missing.UUIDIdentifier};{missing.ExcelName};");
                    Console.WriteLine();
                }
            }



            // are there any orgs in SD that are not created in SOFD?
            var missingSofdOrgs = nuvDeps.Where(d => !Properties.Settings.Default.SOFDUuids.Contains(d.UUIDIdentifier)).ToList();
            if (missingSofdOrgs.Any())
            {
                //throw new Exception("missing sofd orgs");
            }


            // create the inserts for integration database
            var allInserts = new StringBuilder();
            var allTags = new StringBuilder();
            foreach (var nuv in nuvDeps)
            {
                if (nuv.ExcelName == null)
                {
                    // ignore fejl afdeling
                    continue;
                }
                var parent = departments.FirstOrDefault(d => d.UUIDIdentifier == nuv.ParentUUIDIdentifier);
                var shouldHaveVirtual = nuv.ExcelName == parent?.ExcelName;
                var SofdUuid = nuv.UUIDIdentifier;
                var Name = nuv.Name;
                string VirtualUuid;
                string ParentVirtualUuid;
                int Level;
                string insert;
                string sofdTag;
                if (shouldHaveVirtual)
                {
                    // these are the NY tagged in SOFD that has become NUV in SD
                    VirtualUuid = parent.UUIDIdentifier;
                    ParentVirtualUuid = parent.ParentUUIDIdentifier;
                    Level = int.Parse(Regex.Replace(parent.LevelIdentifier, "\\D", ""));
                    sofdTag = $"NY{Level}";
                    if (string.IsNullOrEmpty(ParentVirtualUuid))
                    {
                        insert = $"INSERT INTO dborgunits(SofdUuid, VirtualUuid, ParentVirtualUuid, Name, Level,Created) VALUES('{SofdUuid}', '{VirtualUuid}', NULL, '{Name}', {Level},now());";
                    }
                    else
                    {
                        insert = $"INSERT INTO dborgunits(SofdUuid, VirtualUuid, ParentVirtualUuid, Name, Level,Created) VALUES('{SofdUuid}', '{VirtualUuid}', '{ParentVirtualUuid}', '{Name}', {Level},now());";
                    }
                    
                }
                else
                {
                    // these are the NUV tagged in SOFD that has another NY parent
                    ParentVirtualUuid = parent.UUIDIdentifier;
                    Level = 0;
                    sofdTag = "NUV";
                    insert = $"INSERT INTO dborgunits(SofdUuid, VirtualUuid, ParentVirtualUuid, Name, Level,Created) VALUES('{SofdUuid}', NULL, '{ParentVirtualUuid}', '{Name}', {Level},now());";

                }
                allInserts.AppendLine(insert);

                // create tags in SOFD
                allTags.AppendLine($"INSERT INTO orgunits_tags (orgunit_uuid,tag_id,custom_value) values ('{SofdUuid}',1,'{sofdTag}');");

            }

            

            File.WriteAllText(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\MigrationData\1_dbOrgUnitInserts.sql", allInserts.ToString());
            File.WriteAllText(@"C:\Users\pso\Documents\workspace\sofd-core-sd-integration\assets\datamigration\MigrationData\5_sofdTagInserts.sql", allTags.ToString());

            // figure out if any departments should be moved in SOFD before starting integration
            // try to do a dry run at catch movement messages?

        }
    }

    public class Department
    {
        public string Name { get; set; }
        public string Identifier { get; set; }
        public string UUIDIdentifier { get; set; }
        public string LevelIdentifier { get; set; }
        public string ParentUUIDIdentifier { get; set; }
        public string ExcelName { get; set; }
    }
}
