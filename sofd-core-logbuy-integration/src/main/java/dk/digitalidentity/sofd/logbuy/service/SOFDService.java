package dk.digitalidentity.sofd.logbuy.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import dk.digitalidentity.sofd.logbuy.config.SofdConfiguration;
import dk.digitalidentity.sofd.logbuy.service.model.Affiliation;
import dk.digitalidentity.sofd.logbuy.service.model.Person;
import dk.digitalidentity.sofd.logbuy.service.model.PersonsEmbedded;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SOFDService {
	private static final int pageSize = 1000;

    @Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private SofdConfiguration config;
	
	public List<Person> getPersons() throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders());
		String url = config.getUrl();

		String query = "/api/v2/persons?size=" + pageSize;
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonsEmbedded> response = restTemplate.exchange(url + query + "&page=" + page, HttpMethod.GET, request, PersonsEmbedded.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.warn("Failed to fetch list of persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
				throw new Exception("Failed to fetch a list of Persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			
			if (response.getBody() != null) {
				empty = (response.getBody().getPersons() == null || response.getBody().getPersons().size() == 0);
				if (!empty) {
					result.addAll(response.getBody().getPersons());
					page += 1;
				}
			} else {
				log.warn("Response body was null");
			}
			
		}
		while (!empty);
		
		result = result.stream().filter(p -> p.getUsers().stream().filter(u -> u.getUserType().equals("EXCHANGE")).count() > 0 && p.getAffiliations().stream().filter(a -> activeAffiliation(a)).count() > 0).collect(Collectors.toList());
		return result;
	}

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("apiKey", config.getApiKey());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");
        return headers;
    }
    
    private boolean activeAffiliation(Affiliation affiliation) {
		var tomorrow = LocalDate.now().plusDays(1);
		var yesterday = LocalDate.now().minusDays(1);

		var isActive = (affiliation.getStartDate() == null || affiliation.getStartDate().isBefore(tomorrow)); // check start date
		isActive &= (affiliation.getStopDate() == null || affiliation.getStopDate().isAfter(yesterday)); // check stop date
		isActive &= (affiliation.getEmploymentTerms() != null && (affiliation.getEmploymentTerms().equals("00") || affiliation.getEmploymentTerms().equals("01"))); // check valid employment

		return isActive;
    }
}
