package dk.digitalidentity.sofd.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.core.model.OrgUnit;
import dk.digitalidentity.sofd.core.model.OrgUnitWrapper;
import dk.digitalidentity.sofd.core.model.Person;
import dk.digitalidentity.sofd.core.model.PersonWrapper;
import dk.digitalidentity.sofd.dao.model.Municipality;

@Service
public class CoreService {

	@Autowired
	private RestTemplate restTemplate;

	public List<OrgUnit> getOrgUnits(Municipality municipality) throws Exception {
		List<OrgUnit> result = new ArrayList<>();

		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = municipality.getSofdUrl() + "/api/v2/orgUnits?size=1000";

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<OrgUnitWrapper> response = restTemplate.exchange(query + "&page=" + page, HttpMethod.GET, request, OrgUnitWrapper.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch a list of orgunits. " + response.getStatusCode() + ", response=" + response.getBody());
			}
			
			result.addAll(response.getBody().getOrgUnits());

			empty = (response.getBody().getOrgUnits().size() == 0);			
			page += 1;
		}
		while (!empty);

		return result;
	}

	public List<Person> getPersons(Municipality municipality) throws Exception {
		List<Person> result = new ArrayList<>();

		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = municipality.getSofdUrl() + "/api/v2/persons?size=1000";

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonWrapper> response = restTemplate.exchange(query + "&page=" + page, HttpMethod.GET, request, PersonWrapper.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch a list of Persons. " + response.getStatusCode() + ", response=" + response.getBody());
			}
			
			result.addAll(response.getBody().getPersons());

			empty = (response.getBody().getPersons().size() == 0);			
			page += 1;
		}
		while (!empty);

		return result;
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