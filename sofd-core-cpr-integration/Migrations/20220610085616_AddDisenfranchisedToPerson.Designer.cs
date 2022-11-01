﻿// <auto-generated />
using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Migrations;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using SofdCprIntegration;

namespace SofdCprIntegration.Migrations
{
    [DbContext(typeof(PersonContext))]
    [Migration("20220610085616_AddDisenfranchisedToPerson")]
    partial class AddDisenfranchisedToPerson
    {
        protected override void BuildTargetModel(ModelBuilder modelBuilder)
        {
#pragma warning disable 612, 618
            modelBuilder
                .HasAnnotation("ProductVersion", "3.1.19")
                .HasAnnotation("Relational:MaxIdentifierLength", 64);

            modelBuilder.Entity("SofdCprIntegration.Controllers.Child", b =>
                {
                    b.Property<long>("Id")
                        .ValueGeneratedOnAdd()
                        .HasColumnType("bigint");

                    b.Property<string>("Cpr")
                        .HasColumnType("varchar(10) CHARACTER SET utf8mb4")
                        .HasMaxLength(10);

                    b.Property<long?>("ParentId")
                        .HasColumnType("bigint");

                    b.HasKey("Id");

                    b.HasIndex("ParentId");

                    b.ToTable("Child");
                });

            modelBuilder.Entity("SofdCprIntegration.Controllers.Person", b =>
                {
                    b.Property<long>("Id")
                        .ValueGeneratedOnAdd()
                        .HasColumnType("bigint");

                    b.Property<int>("AddressProtected")
                        .HasColumnType("int");

                    b.Property<string>("City")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("Country")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("Cpr")
                        .HasColumnType("varchar(10) CHARACTER SET utf8mb4")
                        .HasMaxLength(10);

                    b.Property<DateTime>("Created")
                        .HasColumnType("datetime(6)");

                    b.Property<bool>("Disenfranchised")
                        .HasColumnType("tinyint(1)");

                    b.Property<string>("Firstname")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<bool>("IsDead")
                        .HasColumnType("tinyint(1)");

                    b.Property<DateTime>("LastUsed")
                        .HasColumnType("datetime(6)");

                    b.Property<string>("Lastname")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("Localname")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("PostalCode")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("Street")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.HasKey("Id");

                    b.HasIndex("Cpr")
                        .IsUnique();

                    b.ToTable("Person");
                });

            modelBuilder.Entity("SofdCprIntegration.LastSync", b =>
                {
                    b.Property<long>("Id")
                        .ValueGeneratedOnAdd()
                        .HasColumnType("bigint");

                    b.Property<DateTime>("LastSyncDate")
                        .HasColumnType("datetime(6)");

                    b.HasKey("Id");

                    b.ToTable("LastSync");
                });

            modelBuilder.Entity("SofdCprIntegration.Controllers.Child", b =>
                {
                    b.HasOne("SofdCprIntegration.Controllers.Person", "Parent")
                        .WithMany("Children")
                        .HasForeignKey("ParentId")
                        .OnDelete(DeleteBehavior.Cascade);
                });
#pragma warning restore 612, 618
        }
    }
}
