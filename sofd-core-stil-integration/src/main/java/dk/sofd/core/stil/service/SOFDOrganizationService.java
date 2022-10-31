package dk.sofd.core.stil.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.sofd.core.stil.dao.model.Municipality;
import dk.sofd.core.stil.service.model.Person;
import dk.sofd.core.stil.service.model.PersonsEmbedded;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SOFDOrganizationService {
	private static final int pageSize = 1000;

    @Autowired
    private RestTemplate restTemplate;

	public List<Person> getPersons(Municipality municipality) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String url = municipality.getSofdUrl();

		String query = "/v2/persons?size=" + pageSize;
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonsEmbedded> response = restTemplate.exchange(url + query + "&page=" + page, HttpMethod.GET, request, PersonsEmbedded.class);
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
	
    public Collection<Person> getPersons(String cpr, Municipality municipality) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String url = municipality.getSofdUrl();
		String query = "/v2/persons/byCpr/" + cpr;
		
		try {
			ResponseEntity<Person> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Person.class);
			
			return Collections.singletonList(response.getBody());
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() != 404) {
				throw new Exception("Failed to fetch person. " + ex.getRawStatusCode());
			}
		}

		return new ArrayList<Person>();
    }

	public void create(Person person, Municipality municipality) throws Exception {
        log.info(municipality.getName() + ": Creating person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<String> response = restTemplate.exchange(municipality.getSofdUrl() + "/v2/persons", HttpMethod.POST, request, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		if (response.getStatusCodeValue() >= 200 && response.getStatusCodeValue() <= 299) {
			try {
				@SuppressWarnings("unused")
				Person createdPerson = objectMapper.readValue(response.getBody(), Person.class);
			}
			catch (Exception e) {
				log.error("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
		}
		else {
			if (response.getStatusCodeValue() == 400) {
				// we only warn on creation validation errors to mitigate cloudwatch alarm spam
				log.warn("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			else {
				log.error("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
		}
	}

	public void update(Person person, Municipality municipality) throws Exception {
        log.info(municipality.getName() + ": Patching person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<String> response = restTemplate.exchange(municipality.getSofdUrl() + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 399) {
			log.error("Failed to update Person " + person.getFirstname() + " " + person.getSurname() + ". " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");
		headers.add("Accept","application/json");

		return headers;
	}
}
