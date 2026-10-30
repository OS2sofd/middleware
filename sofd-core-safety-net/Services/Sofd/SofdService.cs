using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using sofd_core_safety_net.Services.Sofd.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace sofd_core_safety_net.Services.Sofd
{
    internal class SofdService : ServiceBase<SofdService>
    {
        private readonly Uri baseUri;
        private readonly string apiKey;
        private readonly long orgUnitPageSize;
        private readonly long personsPageSize;
        private readonly long personsPageCount;
        private readonly List<String> orgUnitBlackList;
        private readonly Regex positionNameExcludeRegex;
        private readonly JsonSerializerSettings jsonSerializerSettings = new JsonSerializerSettings() { NullValueHandling = NullValueHandling.Ignore };

        public SofdService(IServiceProvider sp) : base(sp)
        {
            baseUri = new Uri(settings.SofdSettings.Url);
            apiKey = settings.SofdSettings.ApiKey;
            personsPageSize = settings.SofdSettings.PersonsPageSize;
            personsPageCount = settings.SofdSettings.PersonsPageCount;
            orgUnitPageSize = settings.SofdSettings.OrgUnitPageSize;
            orgUnitBlackList = settings.SofdSettings.OrgUnitBlackList;
            if (!String.IsNullOrEmpty(settings.SofdSettings.PositionNameExcludeRegex))
            {
                positionNameExcludeRegex = new Regex(settings.SofdSettings.PositionNameExcludeRegex, RegexOptions.IgnoreCase);
            }            
        }

        public List<OrgUnit> GetOrgUnits()
        {
            logger.LogDebug($"Fetching orgunits from SOFD");
            using var httpClient = GetHttpClient();
            var response = httpClient.GetAsync(new Uri(baseUri, $"api/v2/orgUnits?size={orgUnitPageSize}"));
            response.Wait();
            response.Result.EnsureSuccessStatusCode();
            var responseString = response.Result.Content.ReadAsStringAsync();
            responseString.Wait();

            var getOrgUnitsDto = JsonConvert.DeserializeObject<GetOrgUnitsDto>(responseString.Result, jsonSerializerSettings);
            List<OrgUnit> orgUnits = getOrgUnitsDto.OrgUnits;

            // if we reached max entries on the last page there might be more data in SOFD!
            if (orgUnits.Count == orgUnitPageSize)
            {
                throw new Exception("Not all OrgUnits was fetched from SOFD. Increase PageSize");
            }

            // do some filtering
            orgUnits.RemoveAll(p => p.Deleted);
            if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.OPUS )
            { 
                orgUnits.RemoveAll(p => !p.Master.Equals("OPUS"));
            }            

            logger.LogDebug($"Finshed fetching orgunits from SOFD");
            return orgUnits;
        }

        public List<Person> GetPersons()
        {
            logger.LogDebug($"Fetching persons from SOFD");
            var result = new List<Person>();
            
            for (var page = 0; page < personsPageCount; page++)
            {
                var task = GetPersonsAsync(page);
                task.Wait();
                result.AddRange(task.Result);
            }

            // do some filtering
            result.RemoveAll(p => p.Deleted);

            foreach (var person in result)
            {
                if (settings.SofdSettings.MasterMode == sofd_core_safety_net.Sofd.SofdSettings.MasterModeType.OPUS ) {
                    person.Affiliations.RemoveAll(a => a.Master != "OPUS");
                }
                if (orgUnitBlackList.Count > 0)
                {
                    person.Affiliations.RemoveAll(a => orgUnitBlackList.Contains(a.OrgunitUuid));
                }
                if (positionNameExcludeRegex != null)
                {
                    person.Affiliations.RemoveAll(a => positionNameExcludeRegex.IsMatch(a.PositionName));
                }
            }            

            var tomorrow = DateTime.Today.AddDays(1);
            result.RemoveAll(p => !p.Affiliations.Any(a => !a.Deleted && (a.StopDate == null || a.StopDate >= tomorrow)));

            logger.LogDebug($"Finished fetching persons from SOFD");
            return result;
        }

        private async Task<List<Person>> GetPersonsAsync(int page)
        {
            using var httpClient = GetHttpClient();
            var response = await httpClient.GetAsync(new Uri(baseUri, $"api/v2/persons?size={personsPageSize}&page={page}"));
            response.EnsureSuccessStatusCode();
            var responseString = await response.Content.ReadAsStringAsync();
            var getPersonsDto = JsonConvert.DeserializeObject<GetPersonDto>(responseString, jsonSerializerSettings);
            var result = getPersonsDto.Persons;
            // if we reached max entries on the last page there might be more data in SOFD!
            if (page + 1 == personsPageCount && result.Count == personsPageSize)
            {
                throw new Exception("Not all Persons was fetched from SOFD. Increase PageCount or PageSize");
            }
            return result;
        }

        private HttpClient GetHttpClient()
        {
            var handler = new HttpClientHandler();
            handler.ClientCertificateOptions = ClientCertificateOption.Manual;
            handler.ServerCertificateCustomValidationCallback =
                (httpRequestMessage, cert, cetChain, policyErrors) =>
                {
                    return true;
                };

            var httpClient = new HttpClient(handler);
            httpClient.DefaultRequestHeaders.Add("ApiKey", apiKey);
            return httpClient;
        }

    }
}
