package dk.sofd.organization.ad.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import dk.sofd.organization.ad.security.MunicipalityHolder;
import dk.sofd.organization.ad.service.model.OrgUnit;
import dk.sofd.organization.ad.service.model.Person;
import dk.sofd.organization.ad.service.model.PersonsEmbedded;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableCaching
@Slf4j
public class SOFDOrganizationService {
	private static final int pageSize = 1000;

    @Autowired
    private RestTemplate restTemplate;

	public void postPhoto(String personUuid, byte[] photo) {
		log.info(MunicipalityHolder.get().getName() + ": Posting photo for person with uuid={}", personUuid);
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", MunicipalityHolder.get().getSofdApiKey());
		headers.add("Content-Type", "application/octet-stream");
		headers.add("ClientVersion", MunicipalityHolder.get().getClientVersion());
		headers.add("TlsVersion", MunicipalityHolder.get().getTlsVersion());
		
		HttpEntity<byte[]> request = new HttpEntity<>(photo, headers);
		try {
			restTemplate.exchange(getUrl() + "/photo/" + personUuid, HttpMethod.POST, request, String.class);
		}
		catch (HttpStatusCodeException ex) {
			log.error(MunicipalityHolder.get().getName() + ": Failed to post Photo for person with uuid " + personUuid + ", response=" + ex.getResponseBodyAsString());
		}
	}

	public void deletePhoto(String personUuid) {
		log.info(MunicipalityHolder.get().getName() + ": Deleting photo for person with uuid={}", personUuid);
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", MunicipalityHolder.get().getSofdApiKey());
		headers.add("ClientVersion", MunicipalityHolder.get().getClientVersion());
		headers.add("TlsVersion", MunicipalityHolder.get().getTlsVersion());

		HttpEntity<String> request = new HttpEntity<>(headers);
		try {
			restTemplate.exchange(getUrl() + "/photo/" + personUuid, HttpMethod.DELETE, request, String.class);
		}
		catch (HttpStatusCodeException ex) {
			log.error(MunicipalityHolder.get().getName() + ": Failed to delete Photo for person with uuid " + personUuid + ", response=" + ex.getResponseBodyAsString());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
    @SneakyThrows
    @Cacheable(value = "orgUnits")
    public String getOrgUnitUuidByMasterId(String masterId) {
        HttpEntity request = new HttpEntity(getHeaders());
        String query = "/v2/orgUnits/byMasterId/" + masterId;

        try {
            ResponseEntity<OrgUnit> response = restTemplate.exchange(getUrl() + query, HttpMethod.GET, request, OrgUnit.class);
            if (response.getStatusCodeValue() != 200) {
            	log.warn("Could not find OrgUnit with masterId " + masterId);

            	return null;
            }

            return response.getBody().getUuid();
        }
        catch (HttpStatusCodeException ex) {
            log.warn(MunicipalityHolder.get().getName() + ": Failed to fetch OrgUnit. " + ex.getRawStatusCode() + ", response=" + ex.getResponseBodyAsString());

            return null;
        }
    }

	public List<Person> getPersons() throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders());
		String url = getUrl();

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
	
    public Collection<Person> getPersons(String cpr) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders());
		String url = getUrl();
		String query = "/v2/persons/byCpr/" + cpr;
		
		try {
			ResponseEntity<Person> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Person.class);
			
			return Collections.singletonList(response.getBody());
		}
		catch (HttpStatusCodeException ex) {
			if (ex.getRawStatusCode() != 404) {
				throw new Exception("Failed to fetch person. " + ex.getRawStatusCode());
			}
		}

		return new ArrayList<Person>();
    }

	public Person create(Person person) throws Exception {
        log.info(MunicipalityHolder.get().getName() + ": Creating person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders());

        try {
			ResponseEntity<Person> response = restTemplate.exchange(getUrl() + "/v2/persons", HttpMethod.POST, request, Person.class);
			
			return response.getBody();
        }
        catch (HttpStatusCodeException ex) {
        	log.error("Failed to create Person " + person.getFirstname() + " " + person.getSurname() + ". " + ex.getRawStatusCode() + ". " + ex.getResponseBodyAsString());
        }
		
        return null;
	}

	public void deleteUserByADMasterId(String masterId) {
		log.info(MunicipalityHolder.get().getName() + ": Deleting AD user with master_id={}", masterId);

		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", MunicipalityHolder.get().getSofdApiKey());
		headers.add("ClientVersion", MunicipalityHolder.get().getClientVersion());
		headers.add("TlsVersion", MunicipalityHolder.get().getTlsVersion());

		HttpEntity<String> request = new HttpEntity<>(headers);
		try {
			restTemplate.exchange(getUrl() + "/user/deleteUserByADMasterId/" + masterId, HttpMethod.DELETE, request, String.class);
		}
		catch (HttpStatusCodeException ex) {
			if( ex.getStatusCode() == HttpStatus.NOT_FOUND)
			{
				log.info(MunicipalityHolder.get().getName() + ": Could not delete user with master_id " + masterId + " because SOFD version does not support this operation");
			}
			else
			{
				log.error(MunicipalityHolder.get().getName() + ": Failed to delete user with master_id " + masterId + ", response=" + ex.getResponseBodyAsString());
			}
		}
	}

	public void update(Person person) throws Exception {
        log.info(MunicipalityHolder.get().getName() + ": Patching person. uuid={}", person.getUuid());

        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders());

        try {
        	restTemplate.exchange(getUrl() + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
        }
        catch (HttpStatusCodeException ex) {
        	log.error("Failed to update Person " + person.getUuid() + ". " + ex.getRawStatusCode() + ". " + ex.getResponseBodyAsString() + ". Person = " + person.toString());        	
        }
	}

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("apiKey", MunicipalityHolder.get().getSofdApiKey());
        headers.add("Content-Type", "application/json");
        headers.add("ClientVersion", MunicipalityHolder.get().getClientVersion());
        headers.add("TlsVersion", MunicipalityHolder.get().getTlsVersion());

        return headers;
    }

    private String getUrl() {
        return MunicipalityHolder.get().getSofdUrl();
    }

}
