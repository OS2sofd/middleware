using DigitalIdentity.SOFD.Model;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using DigitalIdentity.Utility;
using System.Net;

namespace DigitalIdentity.SOFD
{
    class SOFDService : BaseClass<SOFDService>
    {
        private readonly Uri baseUri;
        private static readonly HttpClient httpClient = new HttpClient();
        public SOFDService(IServiceProvider sp) : base(sp) {
            baseUri = new Uri(appSettings.SOFDSettings.BaseUrl);
            httpClient.DefaultRequestHeaders.Add("ApiKey", appSettings.SOFDSettings.ApiKey);
        }

        public Person UpdatePerson(Person person)
        {
            logger.LogDebug($"Updating person with uuid {person.Uuid}");
            var jsonPerson = JsonConvert.SerializeObject(person);
            var content = new StringContent(jsonPerson.ToString(), Encoding.UTF8, "application/json");
            var response = httpClient.PatchAsync(new Uri(baseUri, $"persons/{person.Uuid}"), content);   
            response.Wait();
            Person result;
            if (response.Result.StatusCode == HttpStatusCode.NotModified)
            {
                logger.LogWarning($"Unnecessary call to UpdatePerson. Person was not modified in SOFD.");
                result = person;
            }
            else
            {
                response.Result.EnsureSuccessStatusCode();
                var responseString = response.Result.Content.ReadAsStringAsync();
                responseString.Wait();
                result = JsonConvert.DeserializeObject<Person>(responseString.Result);
            }
            return result;
        }

        public OrgUnit UpdateOrgUnit(OrgUnit orgUnit)
        {
            logger.LogDebug($"Updating orgUnit with uuid {orgUnit.Uuid}");
            var jsonOrgUnit = JsonConvert.SerializeObject(orgUnit);
            var content = new StringContent(jsonOrgUnit.ToString(), Encoding.UTF8, "application/json");
            var response = httpClient.PatchAsync(new Uri(baseUri, $"orgUnits/{orgUnit.Uuid}"), content);
            response.Wait();
            OrgUnit result;
            if (response.Result.StatusCode == HttpStatusCode.NotModified)
            {
                logger.LogWarning($"Unnecessary call to UpdateOrgUnit. OrgUnit was not modified in SOFD.");
                result = orgUnit;
            }
            else
            {
                response.Result.EnsureSuccessStatusCode();
                var responseString = response.Result.Content.ReadAsStringAsync();
                responseString.Wait();
                result = JsonConvert.DeserializeObject<OrgUnit>(responseString.Result);
            }
            return result;
        }

        public void ClearManager(OrgUnit orgUnit)
        {
            var response = httpClient.GetAsync(new Uri(baseUri, $"orgUnits/{orgUnit.Uuid}/clearManager"));
            response.Wait();
            response.Result.EnsureSuccessStatusCode();
        }

        public Person CreatePerson(Person person)
        {
            logger.LogDebug($"Creating person with cpr {Helper.FormatCprForLog(person.Cpr)}");
            var jsonPerson = JsonConvert.SerializeObject(person);
            var content = new StringContent(jsonPerson.ToString(), Encoding.UTF8, "application/json");            
            var response = httpClient.PostAsync(new Uri(baseUri, "persons"), content);
            response.Wait();
            response.Result.EnsureSuccessStatusCode();
            var responseString = response.Result.Content.ReadAsStringAsync();
            responseString.Wait();
            var result = JsonConvert.DeserializeObject<Person>(responseString.Result);
            return result;
        }

        public List<OrgUnit> GetOrgUnits()
        {
            logger.LogDebug($"Fetching orgunits from SOFD");
            var response = httpClient.GetAsync(new Uri(baseUri, $"orgUnits?size={appSettings.SOFDSettings.GetOrgUnitsPageSize}"));
            response.Wait();
            response.Result.EnsureSuccessStatusCode();
            var responseString = response.Result.Content.ReadAsStringAsync();
            responseString.Wait();

            var getOrgUnitsDto = JsonConvert.DeserializeObject<GetOrgUnitsDto>(responseString.Result);
            var result = getOrgUnitsDto.OrgUnits;
            // if we reached max entries on the last page there might be more data in SOFD!
            if (result.Count == appSettings.SOFDSettings.GetOrgUnitsPageSize)
            {
                throw new Exception("Not all OrgUnits was fetched from SOFD. Increase PageSize");
            }
            return result;

        }

        public Person GetPerson(string cpr)
        {
            logger.LogDebug($"Fetching person with cpr {Helper.FormatCprForLog(cpr)}");
            var response = httpClient.GetAsync(new Uri(baseUri, $"persons/byCpr/{cpr}"));
            response.Wait();
            if( response.Result.StatusCode == HttpStatusCode.NotFound)
            {
                return null;
            }
            response.Result.EnsureSuccessStatusCode();
            var responseString = response.Result.Content.ReadAsStringAsync();
            responseString.Wait();
            var result = JsonConvert.DeserializeObject<Person>(responseString.Result);
            return result;
        }

        public List<Person> GetPersons()
        {
            logger.LogDebug($"Fetching persons from SOFD");
            var result = new List<Person>();
            var tasks = new List<Task<List<Person>>>();
            for (var page = 0; page < appSettings.SOFDSettings.GetPersonsPageCount; page++)
            {
                tasks.Add(GetPersonsAsync(page));
            }            
            Task.WaitAll(tasks.ToArray());
            foreach(var task in tasks)
            {
                result.AddRange(task.Result);
            }
            return result;
        }

        private async Task<List<Person>> GetPersonsAsync(int page)
        {
            var response = await httpClient.GetAsync(new Uri(baseUri, $"persons?size={appSettings.SOFDSettings.GetPersonsPageSize}&page={page}"));
            response.EnsureSuccessStatusCode();
            var responseString = await response.Content.ReadAsStringAsync();            
            var getPersonsDto = JsonConvert.DeserializeObject<GetPersonsDto>(responseString);
            var result = getPersonsDto.Persons;
            // if we reached max entries on the last page there might be more data in SOFD!
            if (page+1 == appSettings.SOFDSettings.GetPersonsPageCount && result.Count == appSettings.SOFDSettings.GetPersonsPageSize)
            {
                throw new Exception("Not all Persons was fetched from SOFD. Increase PageCount or PageSize");
            }
            return result;
        }

    }
}
