using Microsoft.EntityFrameworkCore.Migrations;

namespace SofdCprIntegration.Migrations
{
    public partial class AddDeadToPerson : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "IsDead",
                table: "Person",
                nullable: false,
                defaultValue: false);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "IsDead",
                table: "Person");
        }
    }
}
