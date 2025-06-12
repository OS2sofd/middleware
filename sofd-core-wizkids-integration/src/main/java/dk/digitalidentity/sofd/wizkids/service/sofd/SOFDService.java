package dk.digitalidentity.sofd.wizkids.service.sofd;

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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SOFDService {
	private static final int pageSize = 1000;

    @Autowired
    private RestTemplate restTemplate;
	
	public List<Person> getPersons(Municipality municipality) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
		String url = getUrl(municipality);

		String query = "/v2/persons?size=" + pageSize;
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonsEmbedded> response = restTemplate.exchange(url + query + "&page=" + page, HttpMethod.GET, request, PersonsEmbedded.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch a list of Persons. " + response.getStatusCode().value() + ", response=" + response.getBody());
			}
			
			empty = (response.getBody().getPersons() == null || response.getBody().getPersons().size() == 0);
			if (!empty) {
				result.addAll(response.getBody().getPersons());
				
				log.info(municipality.getName() + " found " + result.size() + " persons");
				page += 1;
			}
		}
		while (!empty);

		return result;
	}
	
	public Person getPerson(String uuid, Municipality municipality) {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
		String url = getUrl(municipality);
		String query = "/v2/persons/" + uuid;
		
		try {
			ResponseEntity<Person> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Person.class);
			
			return response.getBody();
		}
		catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().value() != 404) {
				log.error("Failed to fetch person. " + ex.getStatusCode().value());
			}
		}

		return null;
	}
	
    public Collection<Person> getPersons(String cpr, Municipality municipality) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
		String url = getUrl(municipality);
		String query = "/v2/persons/byCpr/" + cpr;
		
		try {
			ResponseEntity<Person> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Person.class);
			
			return Collections.singletonList(response.getBody());
		}
		catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().value() != 404) {
				throw new Exception("Failed to fetch person. " + ex.getStatusCode().value());
			}
		}

		return new ArrayList<Person>();
    }

	public void update(Person person, Municipality municipality) throws Exception {
        log.info(municipality.getName() + ": Patching person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(municipality));

		ResponseEntity<String> response = restTemplate.exchange(getUrl(municipality) + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCode().value() < 200 || response.getStatusCode().value() > 399) {
			log.error(municipality.getName() + ": Failed to update Person " + person.getUuid() + ". " + response.getStatusCode().value());
		}
	}

	public Person create(Person person, Municipality municipality) throws Exception {
        log.info(municipality.getName() + ": Creating person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(municipality));

        try {
			ResponseEntity<Person> response = restTemplate.exchange(getUrl(municipality) + "/v2/persons", HttpMethod.POST, request, Person.class);
			
			return response.getBody();
        }
        catch (HttpStatusCodeException ex) {
        	ObjectMapper mapper = new ObjectMapper();
        	String payload = mapper.writeValueAsString(person);
        	log.warn("Failed payload: " + payload);
        	log.error("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + ex.getStatusCode().value() + ". " + ex.getResponseBodyAsString());
        }
		
        return null;
	}

    private HttpHeaders getHeaders(Municipality municipality) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("apiKey", municipality.getSofdApiKey());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return headers;
    }

    private String getUrl(Municipality municipality) {
        return municipality.getSofdUrl();
    }
}
