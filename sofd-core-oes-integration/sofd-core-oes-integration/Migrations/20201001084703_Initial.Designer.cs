﻿// <auto-generated />
using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Migrations;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using sofd_core_oes_integration.Database;

namespace sofd_core_oes_integration.Migrations
{
    [DbContext(typeof(DatabaseContext))]
    [Migration("20201001084703_Initial")]
    partial class Initial
    {
        protected override void BuildTargetModel(ModelBuilder modelBuilder)
        {
#pragma warning disable 612, 618
            modelBuilder
                .HasAnnotation("ProductVersion", "3.1.7")
                .HasAnnotation("Relational:MaxIdentifierLength", 64);

            modelBuilder.Entity("sofd_core_oes_integration.Database.Model.DBOrgUnit", b =>
                {
                    b.Property<int>("Id")
                        .ValueGeneratedOnAdd()
                        .HasColumnType("int");

                    b.Property<string>("ADR1")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("ADR2")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("CVR")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("FBENAAR")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("INSTEMAIL")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("INSTNR")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("KTOPDEL")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("PNR")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("SBENAAR")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("SOFDUuid")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("TEKST1")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("TEKST2")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("TLF")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("TXTLIN")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.Property<string>("TXTSTRUK")
                        .HasColumnType("longtext CHARACTER SET utf8mb4");

                    b.HasKey("Id");

                    b.ToTable("OrgUnits");
                });

            modelBuilder.Entity("sofd_core_oes_integration.Database.Model.SynchronizeInfo", b =>
                {
                    b.Property<int>("Id")
                        .ValueGeneratedOnAdd()
                        .HasColumnType("int");

                    b.Property<DateTime>("PersonsLastSync")
                        .HasColumnType("datetime(6)");

                    b.HasKey("Id");

                    b.ToTable("SynchronizeInfo");
                });
#pragma warning restore 612, 618
        }
    }
}