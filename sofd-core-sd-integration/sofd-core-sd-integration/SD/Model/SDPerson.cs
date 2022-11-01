using GetEmploymentChanged;
using System;
using System.Collections.Generic;
using System.Linq;

namespace DigitalIdentity.SD.Model
{
    public class SDPerson
    {
        public List<SDEmployment> Employments { get; set; }
        public string PersonCivilRegistrationIdentifier { get; set; }
        public string PersonGivenName { get; set; }
        public string PersonSurnameName { get; set; }

        public string City { get; set; }
        public string Street { get; set; }
        public string Country { get; set; }
        public string PostalCode { get; set; }

        public bool HasValidAddress()
        {
            return City != null && Street != null && Country != null && PostalCode != null && PostalCode != "0000" && !Street.Contains("*");
        }

        public static SDPerson FromPersonTypeChangedAtDate(GetEmploymentChangedAtDate.PersonTypeChangedAtDate p)
        {
            var result = new SDPerson();
            result.PersonCivilRegistrationIdentifier = p.PersonCivilRegistrationIdentifier;
            result.Employments = p.Employment.Select(e => SDEmployment.FromEmploymentTypeChangedAtDate(e)).ToList();
            return result;
        }

        public static SDPerson FromPersonType(GetPerson.PersonType p)
        {
            var result = new SDPerson();
            result.PersonCivilRegistrationIdentifier = p.PersonCivilRegistrationIdentifier;
            result.PersonGivenName = p.PersonGivenName;
            result.PersonSurnameName = p.PersonSurnameName;
            result.City = p.PostalAddress?.DistrictName;
            result.Street = p.PostalAddress?.StandardAddressIdentifier;
            result.Country = p.PostalAddress?.CountryIdentificationCode.Value;
            result.PostalCode = p.PostalAddress?.PostalCode;
            return result;
        }

        public static SDPerson FromPersonType(GetEmployment.PersonType p)
        {
            var result = new SDPerson();
            result.PersonCivilRegistrationIdentifier = p.PersonCivilRegistrationIdentifier;
            result.Employments = p.Employment.Select(e => SDEmployment.FromEmploymentType(e)).ToList();
            return result;
        }

        public static SDPerson FromPersonTypeChanged(GetEmploymentChanged.PersonType p)
        {
            var result = new SDPerson();
            result.PersonCivilRegistrationIdentifier = p.PersonCivilRegistrationIdentifier;
            result.Employments = p.Employment.Select(e => SDEmployment.FromEmploymentTypeChanged(e)).ToList();
            return result;
        }
    }
}
