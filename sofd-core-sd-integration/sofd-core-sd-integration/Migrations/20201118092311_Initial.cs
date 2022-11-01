using System;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;

namespace sofd_core_sd_integration.Migrations
{
    public partial class Initial : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "DBOrgUnits",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    SofdUuid = table.Column<string>(nullable: true),
                    VirtualUuid = table.Column<string>(nullable: true),
                    ParentVirtualUuid = table.Column<string>(nullable: true),
                    Name = table.Column<string>(nullable: true),
                    Level = table.Column<int>(nullable: false),
                    Street = table.Column<string>(nullable: true),
                    PostalCode = table.Column<string>(nullable: true),
                    City = table.Column<string>(nullable: true),
                    Phone = table.Column<string>(nullable: true),
                    PNumber = table.Column<string>(nullable: true),
                    Created = table.Column<DateTime>(nullable: true),
                    Changed = table.Column<DateTime>(nullable: true),
                    Deleted = table.Column<DateTime>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_DBOrgUnits", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "FailedSyncPersons",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Cpr = table.Column<string>(nullable: true),
                    FailedTimestamp = table.Column<DateTime>(nullable: false),
                    ErrorMessage = table.Column<string>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FailedSyncPersons", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "FullSyncPersons",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Cpr = table.Column<string>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FullSyncPersons", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "MQLogs",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    MessageId = table.Column<string>(nullable: true),
                    Timestamp = table.Column<DateTime>(nullable: false),
                    Operation = table.Column<string>(nullable: true),
                    IsSent = table.Column<bool>(nullable: false),
                    Message = table.Column<string>(nullable: true),
                    OrgUnitUuid = table.Column<string>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MQLogs", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "SynchronizeInfo",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    PersonsLastSync = table.Column<DateTime>(nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_SynchronizeInfo", x => x.Id);
                });
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "DBOrgUnits");

            migrationBuilder.DropTable(
                name: "FailedSyncPersons");

            migrationBuilder.DropTable(
                name: "FullSyncPersons");

            migrationBuilder.DropTable(
                name: "MQLogs");

            migrationBuilder.DropTable(
                name: "SynchronizeInfo");
        }
    }
}
