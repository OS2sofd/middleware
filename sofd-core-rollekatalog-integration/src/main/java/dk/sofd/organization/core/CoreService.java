package dk.sofd.organization.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.sofd.organization.core.model.OrgUnit;
import dk.sofd.organization.core.model.Person;
import dk.sofd.organization.core.model.SyncResult;
import dk.sofd.organization.dao.model.Municipality;

@Service
public class CoreService {

	@Autowired
	private RestTemplate restTemplate;

	public Person[] getPersons(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		
		String url = municipality.getSofdUrl() + "/sync/adgrid/persons";
		if (municipality.isIncludeUniloginUsers()) {
			url += "?includeUniloginUsers=true";
		}

		ResponseEntity<Person[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Person[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return response.getBody();
	}

	public SyncResult getDeltaPersons(Municipality municipality, long deltaOffset) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<SyncResult> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/persons?offset=" + deltaOffset, HttpMethod.GET, request, SyncResult.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		SyncResult syncResult = response.getBody();

		return syncResult;
	}
	
	public OrgUnit[] getOrgUnits(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<OrgUnit[]> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/adgrid/orgunits", HttpMethod.GET, request, OrgUnit[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of orgunits. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return response.getBody();
	}

	public Long getHead(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<Long> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/head", HttpMethod.GET, request, Long.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch the head." + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return response.getBody();
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");
		headers.add("ClientVersion", "1.0.0");
		headers.add("Accept", "application/json");

		return headers;
	}
}
