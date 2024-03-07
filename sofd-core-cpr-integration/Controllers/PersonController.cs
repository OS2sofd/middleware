using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using CprSubscriptionService;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using PersonBaseDataExtendedService;
using SofdCprIntegration.Model;

namespace SofdCprIntegration.Controllers
{
    [ApiController]
    [Produces("application/json")]
    [Route("api/[controller]")]
    public class PersonController : ControllerBase
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private IConfiguration Configuration;
        private PersonContext personContext;

        public PersonController(IConfiguration configuration, PersonContext pc)
        {
            Configuration = configuration;
            personContext = pc;
        }

        [EnableCors("AllowMyOrigin")]
        [HttpGet]
        public ActionResult<Person> Get([FromQuery]string cpr, [FromQuery]string cvr, [FromQuery] Boolean avoidCache, [FromQuery] Boolean useAddressName = false)
        {
            if (!Validate(cpr))
            {
                return BadRequest("Invalid CPR!");
            }

            if (!ValidateCvr(cvr))
            {
                return BadRequest("Invalid CVR!");
            }

            SofdCprIntegration.Controllers.Person localPerson = null;
            string connectionString = Configuration.GetConnectionString("mysql");

            // Lookup in the local db
            if (!string.IsNullOrEmpty(connectionString))
            {
                localPerson = personContext.Person.Where(p => p.Cpr.Equals(cpr)).Include(c => c.Children).FirstOrDefault();
            }

            if (avoidCache && localPerson != null)
            {
                personContext.Person.Remove(localPerson);
                personContext.SaveChanges();
                localPerson = null;
            }

            if (localPerson == null)
            {
                try
                {
                    log.Debug("Read from online CPR register");

                    PersonBaseDataExtendedPortTypeClient client = new PersonBaseDataExtendedPortTypeClient(Configuration["CPRService:serviceUrl"], Configuration["CPRService:certPath"], Configuration["CPRService:certPassword"], "true".Equals(Configuration["CPRService:traceLoggingEnabled"]));
                    PersonLookupRequestType request = new PersonLookupRequestType();
                    request.PNR = cpr;
                    request.AuthorityContext = new AuthorityContextType();
                    request.AuthorityContext.MunicipalityCVR = cvr;
                    PersonLookupResponse response = client.PersonLookupAsync(request).Result;

                    Person person = new Person();

                    var addressName = response.PersonLookupResponse1.persondata.navn.personadresseringsnavn;
                    if (!string.IsNullOrWhiteSpace(addressName) && useAddressName)
                    {
                        var names = new Stack<string>(Regex.Split(addressName, @"\s+"));
                        person.Lastname = names.Pop();
                        person.Firstname = String.Join(" ", names.Reverse());
                    }
                    else
                    {
                        person.Firstname = response.PersonLookupResponse1.persondata.navn.fornavn;
                        if (!string.IsNullOrEmpty(response.PersonLookupResponse1.persondata.navn.mellemnavn))
                        {
                            person.Firstname = person.Firstname + " " + response.PersonLookupResponse1.persondata.navn.mellemnavn;
                        }

                        person.Lastname = response.PersonLookupResponse1.persondata.navn.efternavn;
                    }

                    if (response.PersonLookupResponse1.adresse?.aktuelAdresse?.standardadresse != null)
                    {
                        person.Street = response.PersonLookupResponse1.adresse.aktuelAdresse.standardadresse;
                        person.Localname = response.PersonLookupResponse1.adresse.aktuelAdresse.bynavn;
                        person.PostalCode = response.PersonLookupResponse1.adresse.aktuelAdresse.postnummer;
                        person.City = response.PersonLookupResponse1.adresse.aktuelAdresse.postdistrikt;
                        person.Country = "Danmark";
                    }
                    else if (response.PersonLookupResponse1.adresse?.udrejseoplysninger?.udlandsadresse1 != null)
                    {
                        person.Street = response.PersonLookupResponse1.adresse.udrejseoplysninger.udlandsadresse1;
                        person.Localname = null;

                        StringBuilder postalCodeBuilder = new StringBuilder();
                        StringBuilder cityBuilder = new StringBuilder();
                        foreach (var c in response.PersonLookupResponse1.adresse.udrejseoplysninger.udlandsadresse2.ToCharArray())
                        {
                            if (c >= '0' && c <= '9' && cityBuilder.Length == 0)
                            {
                                postalCodeBuilder.Append(c);
                            }
                            else if (cityBuilder.Length > 0 || c != ' ')
                            {
                                cityBuilder.Append(c);
                            }
                        }

                        person.PostalCode = postalCodeBuilder.ToString();
                        person.City = cityBuilder.ToString();
                        person.Country = response.PersonLookupResponse1.adresse.udrejseoplysninger.udlandsadresse3;
                    }

                    person.AddressProtected = response.PersonLookupResponse1.persondata.adressebeskyttelse.beskyttetSpecified ? response.PersonLookupResponse1.persondata.adressebeskyttelse.beskyttet : false;
                    person.Cpr = cpr;
                    person.LastUsed = DateTime.Now;
                    person.Created = DateTime.Now;
                    person.IsDead = false;
                    person.Gone = false;

                    // create potential BadState
                    bool badStateAdded = false;
                    BadState badState = personContext.BadState.Where(p => p.Cpr.Equals(cpr)).FirstOrDefault();
                    if (badState == null)
                    {
                        badState = new BadState()
                        {
                            Cpr = person.Cpr,
                            Disenfranchised = false,
                            Gone = false,
                            IsDead = false,
                            Tts = DateTime.Now
                        };
                    }
                    else
                    {
                        badStateAdded = true;
                    }

                    if (response.PersonLookupResponse1.persondata?.status?.status != null)
                    {
                        // 90 is "Død", 70 is "Bortkommet"
                        if (90 == response.PersonLookupResponse1.persondata.status.status)
                        {
                            person.IsDead = true;

                            badState.IsDead = true;
                            if (!badStateAdded)
                            {
                                personContext.BadState.Add(badState);
                                badStateAdded = true;
                            }
                            personContext.SaveChanges();
                        }
                        else if (70 == response.PersonLookupResponse1.persondata.status.status)
                        {
                            person.Gone = true;

                            badState.Gone = true;
                            if (!badStateAdded)
                            {
                                personContext.BadState.Add(badState);
                                badStateAdded = true;
                            }
                            personContext.SaveChanges();
                        }
                    }

                    person.Disenfranchised = false;
                    if (response.PersonLookupResponse1.persondata?.umyndiggoerelse?.umyndiggjort == true)
                    {
                        person.Disenfranchised = true;

                        badState.Disenfranchised = true;
                        if (!badStateAdded)
                        {
                            personContext.BadState.Add(badState);
                            badStateAdded = true;
                        }
                        personContext.SaveChanges();
                    }

                    // save to localdb
                    if (!string.IsNullOrEmpty(connectionString))
                    {
                        personContext.Person.Add(person);
                        personContext.SaveChanges();

                        if (response.PersonLookupResponse1?.relationer?.barn != null)
                        {
                            foreach (var c in response.PersonLookupResponse1.relationer.barn)
                            {
                                Child child = new Child();
                                child.Cpr = c.personnummer;
                                child.Parent = person;
                                person.Children.Add(child);
                            }
                        }

                        personContext.SaveChanges();

                        return person;
                    }
                }
                catch (Exception ex)
                {
                    if (ex.Message.Contains("PNR not found"))
                    {
                        return NotFound("Get 'PNR not found' message from Serviceplatform");
                    }

                    log.Warn("Failed to lookup " + Hide(cpr) + " for " + cvr + ". Message: " + ex.Message, ex);

                    return BadRequest(ex.Message);
                }
            }

            log.Debug("Read from CPR cache");

            localPerson.LastUsed = DateTime.Now;
            personContext.SaveChanges();

            return localPerson;
        }

        private string Hide(string cpr)
        {
            return cpr.Substring(0, 6) + "-XXXX";
        }

        private bool ValidateCvr(string cvr)
        {
            // 8 chars exactly
            if (cvr == null || cvr.Length != 8)
            {
                return false;
            }

            // only numbers
            foreach (char c in cvr)
            {
                if (c < '0' || c > '9')
                {
                    return false;
                }
            }

            return true;
        }

        private bool Validate(string cpr)
        {
            // 10 chars exactly
            if (cpr == null || cpr.Length != 10)
            {
                return false;
            }

            // only numbers
            foreach (char c in cpr)
            {
                if (c < '0' || c > '9')
                {
                    return false;
                }
            }

            // first 2 should be 1-31
            int days = Int32.Parse(cpr.Substring(0, 2));
            if (days < 1 || days > 31)
            {
                return false;
            }

            // second 2 should be 1-12
            int months = Int32.Parse(cpr.Substring(2, 2));
            if (months < 1 || months > 12)
            {
                return false;
            }

            return true;
        }
    }
}
