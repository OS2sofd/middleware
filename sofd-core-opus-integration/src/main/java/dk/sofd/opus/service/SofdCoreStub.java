package dk.sofd.opus.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import dk.sofd.opus.dao.model.Municipality;
import dk.sofd.opus.service.model.Affiliation;
import dk.sofd.opus.service.model.OpusExtraAffiliations;
import dk.sofd.opus.service.model.OpusFilterRulesDTO;
import dk.sofd.opus.service.model.OrgUnit;
import dk.sofd.opus.service.model.OrgUnitsDto2;
import dk.sofd.opus.service.model.Person;
import dk.sofd.opus.service.model.PersonsDto2;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SofdCoreStub {

	@Value("${sofd.core.pagesize:1000}")
	private int pageSize;

	@Autowired
	private RestTemplate restTemplate;

	public Map<String, List<String>> getExtraAffiliations(Municipality municipality) {
		Map<String, List<String>> result = new HashMap<>();

		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();
		
		HttpEntity<String> request = new HttpEntity<>(getHeaders(apiKey));

		String query = "/opusautoaffiliations";
		try {
			ResponseEntity<OpusExtraAffiliations> response = restTemplate.exchange(url + query, HttpMethod.GET, request, OpusExtraAffiliations.class);

			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch OPUS filter rules. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			
			String value = response.getBody().getValue();
			if (!StringUtils.isEmpty(value)) {
				String[] tokens = value.split(";");
				
				for (String token : tokens) {
					String[] pair = token.split("=");
					if (pair.length == 2) {
						
						if (result.containsKey(pair[0])) {
							result.get(pair[0]).add(pair[1]);
						}
						else {
							List<String> list = new ArrayList<>();
							list.add(pair[1]);

							result.put(pair[0], list);
						}
					}
				}
			}
		}
		catch (Exception ex) {
			log.warn("Failed to fetch OPUS Extra Affiliation Rules: " + ex.getMessage());
		}
		
		return result;
	}

	public List<Affiliation> findOpusAffiliations(Municipality municipality, String uuid) throws Exception {
		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();

		HttpEntity<String> request = new HttpEntity<>(getHeaders(apiKey));
		String query = "/v2/affiliations/OPUS/" + uuid;

		ResponseEntity<Affiliation[]> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Affiliation[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of Affiliations. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		List<Affiliation> result = new ArrayList<>();
		for (Affiliation affiliation : response.getBody()) {
			result.add(affiliation);
		}
		
		return result;
	}
	
	public OpusFilterRulesDTO getSettings(Municipality municipality) throws Exception {
		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();
		
		HttpEntity<String> request = new HttpEntity<>(getHeaders(apiKey));

		String query = "/opusfilters";
		ResponseEntity<OpusFilterRulesDTO> response = restTemplate.exchange(url + query, HttpMethod.GET, request, OpusFilterRulesDTO.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			
			// TODO: remove this once all municipalities are updated to the latest version of SOFD Core,
			// and this API is available default to not filtering
			if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				OpusFilterRulesDTO filterRules = new OpusFilterRulesDTO();
				filterRules.setEnabled(false);
				
				return filterRules;
			}

			throw new Exception("Failed to fetch OPUS filter rules. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return response.getBody();		
	}

	public List<Person> getPersons(Municipality municipality) throws Exception {
		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();

		HttpEntity<String> request = new HttpEntity<>(getHeaders(apiKey));
		String query = "/v2/persons?size=" + pageSize;
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonsDto2> response = restTemplate.exchange(url + query + "&page=" + page, HttpMethod.GET, request, PersonsDto2.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch a list of Persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			
			result.addAll(response.getBody().getPersons());

			empty = (response.getBody().getPersons().size() == 0);			
			page += 1;
		}
		while (!empty);

		return result;
	}

	public List<OrgUnit> getOrgUnits(Municipality municipality) throws Exception {
		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();

		HttpEntity<String> request = new HttpEntity<>(getHeaders(apiKey));
		String query = "/v2/orgUnits?size=" + pageSize;
		List<OrgUnit> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<OrgUnitsDto2> response = restTemplate.exchange(url + query + "&page=" + page, HttpMethod.GET, request, OrgUnitsDto2.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch a list of OrgUnits. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			
			result.addAll(response.getBody().getOrgUnits());
			
			empty = (response.getBody().getOrgUnits().size() == 0);			
			page += 1;
		}
		while (!empty);

		return result;
	}

	public void update(Person person, Municipality municipality) throws Exception {
		String apiKey = municipality.getApiKey();
		String url = municipality.getUrl();

		log.info("Updating person. uuid={}", person.getUuid());

		HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(apiKey));

		ResponseEntity<String> response = restTemplate.exchange(url + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 399) {
			log.error("Failed to update Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}

	public void create(Person person, Municipality municipality) throws Exception {
		String url = municipality.getUrl();
		String apiKey = municipality.getApiKey();

		log.info("Creating person. uuid={}", person.getUuid());

		HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(apiKey));

		ResponseEntity<String> response = restTemplate.exchange(url + "/v2/persons", HttpMethod.POST, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 299) {
			log.error("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}

	public void delete(OrgUnit orgUnit, Municipality municipality) throws Exception {
		String url = municipality.getUrl();
		String apiKey = municipality.getApiKey();

		log.info("Deleting orgUnit. uuid={}", orgUnit.getUuid());

		HttpEntity<String> request = new HttpEntity<>("{\"deleted\": true}", getHeaders(apiKey));

		ResponseEntity<String> response = restTemplate.exchange(url + "/v2/orgUnits/" + orgUnit.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 299) {
			log.error("Failed to delete OrgUnit " + orgUnit.getUuid() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}
	
	public void createNotifications(Set<JSONObject> missingLosIDS, Municipality municipality) throws Exception {
		HttpEntity<Set<JSONObject>> req = new HttpEntity<>(missingLosIDS, getHeaders(municipality.getApiKey()));
		ResponseEntity<String> response = restTemplate.exchange(municipality.getUrl() + "/notifications", HttpMethod.POST, req, String.class);

		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 299) {
			log.error("Failed to create notifications via API for Sofd: " + response.getBody());
		}
	}

	public void create(OrgUnit orgUnit, Municipality municipality) throws Exception {
		String url = municipality.getUrl();
		String apiKey = municipality.getApiKey();

		log.info("Creating orgUnit. uuid={}", orgUnit.getUuid());

		HttpEntity<OrgUnit> request = new HttpEntity<>(orgUnit, getHeaders(apiKey));

		ResponseEntity<String> response = restTemplate.exchange(url + "/v2/orgUnits", HttpMethod.POST, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 299) {
			log.error("Failed to create OrgUnit " + orgUnit.getMasterId() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}

	public void update(OrgUnit orgUnit, Municipality municipality) throws Exception {
		String url = municipality.getUrl();
		String apiKey = municipality.getApiKey();

		log.info("Updating orgUnit. uuid={}", orgUnit.getUuid());

		HttpEntity<OrgUnit> request = new HttpEntity<>(orgUnit, getHeaders(apiKey));

		ResponseEntity<String> response = restTemplate.exchange(url + "/v2/orgUnits/" + orgUnit.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 399) {
			log.error("Failed to update OrgUnit " + orgUnit.getMasterId() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");
		headers.add("ClientVersion", "1.0.0");

		return headers;
	}
}
