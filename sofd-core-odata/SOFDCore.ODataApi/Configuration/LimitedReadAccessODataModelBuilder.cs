using Microsoft.AspNet.OData.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.OData.Edm;
using SOFDCore.ODataApi.Models;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;

namespace SOFDCore.ODataApi.Configuration
{
    public class LimitedReadAccessODataModelBuilder
    {
        private readonly IHttpContextAccessor httpContextAccessor;
        private ConcurrentDictionary<string, IEdmModel> modelCache = new ConcurrentDictionary<string, IEdmModel>();
        public LimitedReadAccessODataModelBuilder(IHttpContextAccessor httpContextAccessor)
        {
            this.httpContextAccessor = httpContextAccessor;
        }

        public IEdmModel GetEdmModel()
        {
            var claims = httpContextAccessor.HttpContext.User.Claims;
            var modelKey = string.Join("|", claims.Where(c => c.Type == ClaimTypes.Role).OrderBy(c => c.Value).Select(c => c.Value));
            if (!modelCache.ContainsKey(modelKey))
            {
                modelCache.TryAdd(modelKey, BuildEdmModel());
            }
            return modelCache[modelKey];
        }
        private IEdmModel BuildEdmModel()
        {
            var modelBuilder = new ODataConventionModelBuilder();

            modelBuilder.EntityType<FunctionAssignment>().Property(p => p.StartDate).AsDate();
            modelBuilder.EntityType<FunctionAssignment>().Property(p => p.StopDate).AsDate();
            
            // TODO: some of the API consumers fail if we change these to date-only format
            // Correct solution is to make this change and fix all API consumers
            //modelBuilder.EntityType<Affiliation>().Property(p => p.StartDate).AsDate();
            //modelBuilder.EntityType<Affiliation>().Property(p => p.StopDate).AsDate();

            var personConfig = modelBuilder.EntitySet<Person>("Persons").EntityType;
            // binary data is not exposed directy through odata - only by direct service method
            // this is to force users into using checksum/lastchanged to query photos
            modelBuilder.EntitySet<Photo>("Photo").EntityType.Ignore(p => p.Data);
            var orgUnitConfig = modelBuilder.EntitySet<OrgUnit>("OrgUnits").EntityType;
            var userConfig = modelBuilder.EntitySet<User>("Users").EntityType;
            var affiliationConfig = modelBuilder.EntitySet<Affiliation>("Affiliations").EntityType;
            var functionAssignmentsConfig = modelBuilder.EntitySet<FunctionAssignment>("FunctionAssignments").EntityType;
            var functionConfig = modelBuilder.EntitySet<Function>("Functions").EntityType;

            if (HasRoleClaim("LIMITED_READ_ACCESS"))
            {
                // Handle Person claims
                if (!HasRoleClaim("PERSON_ADDRESS"))
                {
                    personConfig.Ignore(p => p.RegisteredPostAddressId);
                    personConfig.Ignore(p => p.RegisteredPostAddress);
                    personConfig.Ignore(p => p.ResidencePostAddressId);
                    personConfig.Ignore(p => p.ResidencePostAddress);
                }
                if (!HasRoleClaim("PERSON_PHONE"))
                {
                    personConfig.Ignore(p => p.Phones);
                }
                if (!HasRoleClaim("PERSON_AFFILIATIONS"))
                {
                    personConfig.Ignore(p => p.Affiliations);
                }
                if (!HasRoleClaim("PERSON_CPR"))
                {
                    personConfig.Ignore(p => p.Cpr);
                    personConfig.Ignore(p => p.Children);
                }                
                if (!HasRoleClaim("PERSON_USER")) 
                {
                    personConfig.Ignore(p => p.Users);
                }

                // Handle OrgUnit claims
                if (!HasRoleClaim("ORGUNIT_ADDRESS")) 
                {
                    orgUnitConfig.Ignore(o => o.Addresses);
                }
                if (!HasRoleClaim("ORGUNIT_PHONE"))
                {
                    orgUnitConfig.Ignore(o => o.Phones);
                }
                if (!HasRoleClaim("ORGUNIT_AFFILIATIONS")) 
                {
                    orgUnitConfig.Ignore(o => o.Affiliations);
                }
                if (!HasRoleClaim("ORGUNIT_KLE")) 
                {
                    orgUnitConfig.Ignore(o => o.KLEPrimary);
                    orgUnitConfig.Ignore(o => o.KLESecondary);
                }
                if (!HasRoleClaim("ORGUNIT_MANAGER")) 
                {
                    orgUnitConfig.Ignore(o => o.Manager);
                }

                // handle mixed claims
                // The user needs to have both affiliation detail claims to see the details.
                if (!HasRoleClaim("PERSON_AFFILIATIONS_DETAILS") || !HasRoleClaim("ORGUNIT_AFFILIATIONS_DETAILS"))
                {
                    affiliationConfig.Ignore(a => a.EmploymentTerms);
                    affiliationConfig.Ignore(a => a.EmploymentTermsText);
                    affiliationConfig.Ignore(a => a.PayGrade);
                    affiliationConfig.Ignore(a => a.WageStep);
                    affiliationConfig.Ignore(a => a.WorkingHoursDenominator);
                    affiliationConfig.Ignore(a => a.WorkingHoursNumerator);
                }

            }

            modelBuilder.Namespace = "PersonService";
            modelBuilder.EntityType<Person>()
                .Function("DownloadPhoto")
                .Returns<byte[]>();

            var model = modelBuilder.GetEdmModel();
            return model;
        }



        private bool HasRoleClaim(string claimValue)
        {
            var claims = httpContextAccessor.HttpContext.User.Claims;
            return claims.Any(c => c.Type == ClaimTypes.Role && c.Value == claimValue);
        }

    }
}
