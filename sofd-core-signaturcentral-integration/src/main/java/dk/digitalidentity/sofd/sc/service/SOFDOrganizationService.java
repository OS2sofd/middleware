package dk.digitalidentity.sofd.sc.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.sc.security.MunicipalityHolder;
import dk.digitalidentity.sofd.sc.service.model.Person;
import dk.digitalidentity.sofd.sc.service.model.PersonsDto;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableCaching
@Slf4j
public class SOFDOrganizationService {

    @Autowired
    private RestTemplate restTemplate;
    
	public List<Person> getPersons() throws Exception {
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		
		String query = "/v2/persons?size=1000";
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			try {
				ResponseEntity<PersonsDto> response = restTemplate.exchange(getUrl() + query + "&page=" + page, HttpMethod.GET, request, PersonsDto.class);				

				result.addAll(response.getBody().getPersons());
				empty = (response.getBody().getPersons().size() == 0);			
				page += 1;
			}
			catch (HttpStatusCodeException ex) {
				log.error(MunicipalityHolder.get().getName() + ": Failed to get persons. " + ex.getRawStatusCode() + ", response=" + ex.getResponseBodyAsString());
				throw ex;
			}
		}
		while (!empty);

		return result;
	}

	public void update(Person person) throws Exception {
        log.info(MunicipalityHolder.get().getName() + ": Patching person. uuid={}", person.getUuid());
        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders());

        try {
        	restTemplate.exchange(getUrl() + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
        }
        catch (HttpStatusCodeException ex) {
			log.error(MunicipalityHolder.get().getName() + ": Failed to update Person " + person.getMasterId() + ". " + ex.getRawStatusCode() + ", response=" + ex.getResponseBodyAsString());
		}
	}

    private HttpHeaders getHeaders() {
    	HttpHeaders headers = new HttpHeaders();
        headers.add("apiKey", MunicipalityHolder.get().getSofdApiKey());
        headers.add("Content-Type", "application/json");
        headers.add("ClientVersion", MunicipalityHolder.get().getClientVersion());

        return headers;
    }
    
    private String getUrl() {
        return MunicipalityHolder.get().getSofdUrl();
    }
}
