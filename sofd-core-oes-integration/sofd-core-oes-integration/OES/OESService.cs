using DigitalIdentity;
using sofd_core_oes_integration.Database;
using Microsoft.Extensions.DependencyInjection;
using System;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml;
using DocumentFormat.OpenXml.Spreadsheet;
using System.Linq;
using sofd_core_oes_integration.Database.Model;
using System.Collections.Generic;
using System.IO;

namespace sofd_core_oes_integration.OES
{
    public class OESService : BaseClass<OESService>
    {
        private readonly DatabaseContext databaseContext;
        public OESService(IServiceProvider sp) : base(sp)
        {
            databaseContext = sp.GetService<DatabaseContext>();
        }

        public void CheckFailSafe()
        {
            var activeOrgUnitCount = databaseContext.OrgUnits.Count(o => o.SBENAAR == null);
            var expectedOrgUnitCount = appSettings.ActiveOrgUnitFailSafeCount;
            if (activeOrgUnitCount < expectedOrgUnitCount) {
                throw new Exception($"ActiveOrgUnitFailSafeCount not reached. Expected at least {expectedOrgUnitCount} active OrgUnits, but found only {activeOrgUnitCount}");
            }
        }

        public Stream GenerateAdmOrgExcel()
        {

            var memoryStream = new MemoryStream();
            //using SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.Create(filePath, SpreadsheetDocumentType.Workbook);
            using var spreadsheetDocument = SpreadsheetDocument.Create(memoryStream, SpreadsheetDocumentType.Workbook);

            WorkbookPart workbookpart = spreadsheetDocument.AddWorkbookPart();
            workbookpart.Workbook = new Workbook();
            WorksheetPart worksheetPart = workbookpart.AddNewPart<WorksheetPart>();
            SheetData sheetData = new SheetData();
            worksheetPart.Worksheet = new Worksheet(sheetData);
            Sheets sheets = spreadsheetDocument.WorkbookPart.Workbook.AppendChild(new Sheets());
            Sheet sheet = new Sheet()
            {
                Id = spreadsheetDocument.WorkbookPart.
                GetIdOfPart(worksheetPart),
                SheetId = 1,
                Name = "Sheet1"
            };
            // Add headers
            uint rowIndex = 0;
            Row headerRow = new Row() { RowIndex = ++rowIndex };
            headerRow.Append(new Cell() { CellReference = $"A{rowIndex}", CellValue = new CellValue("KTOPDEL"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"B{rowIndex}", CellValue = new CellValue("TXTSTRUK"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"C{rowIndex}", CellValue = new CellValue("TXTLIN"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"D{rowIndex}", CellValue = new CellValue("FBENAAR"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"E{rowIndex}", CellValue = new CellValue("SBENAAR"), DataType = CellValues.String });
            sheetData.Append(headerRow);

            // Add data
            foreach (var dbOrgUnit in databaseContext.OrgUnits.OrderBy(o => o.INSTNR).ThenBy(o => o.FBENAAR))
            {
                Row dataRow = new Row() { RowIndex = ++rowIndex };
                dataRow.Append(new Cell() { CellReference = $"A{rowIndex}", CellValue = new CellValue(dbOrgUnit.KTOPDEL), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"B{rowIndex}", CellValue = new CellValue(dbOrgUnit.TXTSTRUK.Replace("-","")), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"C{rowIndex}", CellValue = new CellValue(dbOrgUnit.TXTLIN ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"D{rowIndex}", CellValue = new CellValue(dbOrgUnit.FBENAAR), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"E{rowIndex}", CellValue = new CellValue(dbOrgUnit.SBENAAR ?? ""), DataType = CellValues.String });
                sheetData.Append(dataRow);
            }

            sheets.Append(sheet);
            workbookpart.Workbook.Save();
            spreadsheetDocument.Close();
            return memoryStream;
        }

        public Stream GenerateInstitutionExcel()
        {

            var memoryStream = new MemoryStream();
            using var spreadsheetDocument = SpreadsheetDocument.Create(memoryStream, SpreadsheetDocumentType.Workbook);

            WorkbookPart workbookpart = spreadsheetDocument.AddWorkbookPart();
            workbookpart.Workbook = new Workbook();
            WorksheetPart worksheetPart = workbookpart.AddNewPart<WorksheetPart>();
            SheetData sheetData = new SheetData();
            worksheetPart.Worksheet = new Worksheet(sheetData);
            Sheets sheets = spreadsheetDocument.WorkbookPart.Workbook.AppendChild(new Sheets());
            Sheet sheet = new Sheet()
            {
                Id = spreadsheetDocument.WorkbookPart.
                GetIdOfPart(worksheetPart),
                SheetId = 1,
                Name = "Sheet1"
            };
            // Add headers
            uint rowIndex = 0;
            Row headerRow = new Row() { RowIndex = ++rowIndex };
            headerRow.Append(new Cell() { CellReference = $"A{rowIndex}", CellValue = new CellValue("INSTNR"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"B{rowIndex}", CellValue = new CellValue("CVR"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"C{rowIndex}", CellValue = new CellValue("TEKST1"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"D{rowIndex}", CellValue = new CellValue("TEKST2"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"E{rowIndex}", CellValue = new CellValue("ADR1"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"F{rowIndex}", CellValue = new CellValue("ADR2"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"G{rowIndex}", CellValue = new CellValue("PNR"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"H{rowIndex}", CellValue = new CellValue("TLF"), DataType = CellValues.String });
            headerRow.Append(new Cell() { CellReference = $"I{rowIndex}", CellValue = new CellValue("INSTEMAIL"), DataType = CellValues.String });
            sheetData.Append(headerRow);

            // Add data
            // remove duplicates (old versions with same INSTNR)
            var uniqueOrgUnits = new Dictionary<string, DBOrgUnit>();
            foreach (var dbOrgUnit in databaseContext.OrgUnits.OrderBy(o => o.FBENAAR).ThenByDescending(o => o.TXTSTRUK))
            {
                uniqueOrgUnits[dbOrgUnit.INSTNR] = dbOrgUnit;
            }
            foreach (var dbOrgUnit in uniqueOrgUnits.Values.OrderBy(o => o.INSTNR))
            {
                Row dataRow = new Row() { RowIndex = ++rowIndex };
                dataRow.Append(new Cell() { CellReference = $"A{rowIndex}", CellValue = new CellValue(dbOrgUnit.INSTNR), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"B{rowIndex}", CellValue = new CellValue(dbOrgUnit.CVR), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"C{rowIndex}", CellValue = new CellValue(dbOrgUnit.TEKST1 ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"D{rowIndex}", CellValue = new CellValue(dbOrgUnit.TEKST2 ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"E{rowIndex}", CellValue = new CellValue(dbOrgUnit.ADR1 ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"F{rowIndex}", CellValue = new CellValue(dbOrgUnit.ADR2 ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"G{rowIndex}", CellValue = new CellValue(dbOrgUnit.PNR ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"H{rowIndex}", CellValue = new CellValue(dbOrgUnit.TLF ?? ""), DataType = CellValues.String });
                dataRow.Append(new Cell() { CellReference = $"I{rowIndex}", CellValue = new CellValue(dbOrgUnit.INSTEMAIL ?? ""), DataType = CellValues.String });
                sheetData.Append(dataRow);
            }

            sheets.Append(sheet);
            workbookpart.Workbook.Save();
            spreadsheetDocument.Close();
            return memoryStream;
        }
    }
}