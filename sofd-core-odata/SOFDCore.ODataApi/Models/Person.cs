namespace SOFDCore.ODataApi.Models
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    /// <summary>
    /// Represents a person.
    /// </summary>
    [Table("view_odata_person")]
    public class Person
    {
        [Key]
        public string Uuid { get; set; }
        public string Master { get; set; }
        public bool? Deleted { get; set; }
        public DateTime? Created { get; set; }
        public DateTime? LastChanged { get; set; }
        public string Firstname { get; set; }
        public string Surname { get; set; }
        public string Cpr { get; set; }
        public string ChosenName { get; set; }
        public DateTime? AnniversaryDate { get; set; }
        public string LocalExtensions { get; set; }

        [ForeignKey("RegisteredPostAddress")]
        public long? RegisteredPostAddressId { get; set; }
        public PersonPost RegisteredPostAddress { get; set; }

        [ForeignKey("ResidencePostAddress")]
        public long? ResidencePostAddressId { get; set; }
        public PersonPost ResidencePostAddress { get; set; }

        public string KeyWords { get; set; }
        public string Notes { get; set; }
        public bool? TaxedPhone { get; set; }
        public bool? DisableAccountOrders { get; set; }
        public bool? ForceStop { get; set; }
        public string CalculatedName { get { return !String.IsNullOrEmpty(this.ChosenName) ? this.ChosenName : this.Firstname + " " + this.Surname; } set { } }
        public Leave Leave { get; set; }

        public ICollection<User> Users { get; set; }
        public ICollection<DisabledUser> DisabledUsers { get; set; }
        public ICollection<Affiliation> Affiliations { get; set; }
        public ICollection<PersonPhone> Phones{ get; set; }
        public ICollection<SubstituteAssignment> Substitutes { get; set; }
        public Photo Photo { get; set; }
        public ICollection<PersonChild> Children { get; set; }
        public ICollection<AuthorizationCode> AuthorizationCodes{ get; set; }

    }
}