using System;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;

namespace sofd_core_oes_integration.Migrations
{
    public partial class Initial : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "OrgUnits",
                columns: table => new
                {
                    Id = table.Column<int>(nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    SOFDUuid = table.Column<string>(nullable: true),
                    KTOPDEL = table.Column<string>(nullable: true),
                    TXTSTRUK = table.Column<string>(nullable: true),
                    TXTLIN = table.Column<string>(nullable: true),
                    FBENAAR = table.Column<string>(nullable: true),
                    SBENAAR = table.Column<string>(nullable: true),
                    INSTNR = table.Column<string>(nullable: true),
                    CVR = table.Column<string>(nullable: true),
                    TEKST1 = table.Column<string>(nullable: true),
                    TEKST2 = table.Column<string>(nullable: true),
                    ADR1 = table.Column<string>(nullable: true),
                    ADR2 = table.Column<string>(nullable: true),
                    PNR = table.Column<string>(nullable: true),
                    TLF = table.Column<string>(nullable: true),
                    INSTEMAIL = table.Column<string>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_OrgUnits", x => x.Id);
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
                name: "OrgUnits");

            migrationBuilder.DropTable(
                name: "SynchronizeInfo");
        }
    }
}
