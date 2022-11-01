using Microsoft.EntityFrameworkCore.Migrations;

namespace SofdCprIntegration.Migrations
{
    public partial class AddDisenfranchisedToPerson : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "Disenfranchised",
                table: "Person",
                nullable: false,
                defaultValue: false);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Disenfranchised",
                table: "Person");
        }
    }
}
