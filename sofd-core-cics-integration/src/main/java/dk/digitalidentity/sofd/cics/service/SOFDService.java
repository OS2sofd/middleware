package dk.digitalidentity.sofd.cics.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.model.AccountOrder;
import dk.digitalidentity.sofd.cics.service.model.AccountOrderResponse;
import dk.digitalidentity.sofd.cics.service.model.AccountOrderStatus;
import dk.digitalidentity.sofd.cics.service.model.DeltaSync;
import dk.digitalidentity.sofd.cics.service.model.OrgUnit;
import dk.digitalidentity.sofd.cics.service.model.Person;
import dk.digitalidentity.sofd.cics.service.model.PersonsEmbedded;
import lombok.extern.slf4j.Slf4j;

@EnableCaching
@EnableScheduling
@Service
@Slf4j
public class SOFDService {
	private static final String[] orderTypes = { "CREATE", "DEACTIVATE", "DELETE" };
	private static final int pageSize = 1000;

    @Autowired
    private RestTemplate restTemplate;

	@Autowired
	private SOFDService self;

	public long getHead(Municipality municipality) {
        HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
        String url = getUrl(municipality);
		String query = "/sync/head";

		try {
			ResponseEntity<Long> response = restTemplate.exchange(url + query, HttpMethod.GET, request, Long.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("Failed to get head for " + municipality.getName() + ": " + response.getStatusCodeValue());
				
				return 0L;
			}
			
			return response.getBody();
		}
		catch (HttpStatusCodeException ex) {
			log.error("Failed to get head for " + municipality.getName() + ": " + ex.getRawStatusCode());
		}

		return 0L;
	}
	
	public Set<String> getDeltaPersons(Municipality municipality, long head) {
        HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
        String url = getUrl(municipality);
		String query = "/sync/persons?offset=" + head;

		try {
			var response = restTemplate.exchange(url + query, HttpMethod.GET, request, DeltaSync.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("Failed to get deltasync for " + municipality.getName() + ": " + response.getStatusCodeValue());
				
				return new HashSet<String>();
			}

			return response.getBody().getUuids().stream().map(c -> c.getUuid()).collect(Collectors.toSet());
		}
		catch (HttpStatusCodeException ex) {
			log.error("Failed to get deltasync for " + municipality.getName() + ": " + ex.getRawStatusCode());
		}

		return new HashSet<String>();
	}

	public void setStatusOnOrders(Municipality municipality, List<AccountOrderStatus> result) {
        HttpEntity<List<AccountOrderStatus>> request = new HttpEntity<>(result, getHeaders(municipality));
        String url = getUrl(municipality);
        String query = "/account/KSPCICS/setStatus";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url + query, HttpMethod.POST, request, String.class);
            if (response.getStatusCodeValue() != 200) {
            	log.error("Could not set accountOrder status for municipality: " + municipality.getName() + ". HTTP " + response.getStatusCodeValue());
            }
        }
        catch (HttpStatusCodeException ex) {
        	log.error("Could not set accountOrder status for municipality: " + municipality.getName() + ". HTTP " + ex.getRawStatusCode());
        }
	}

    public List<AccountOrder> getPendingOrders(Municipality municipality) {
        HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
        String url = getUrl(municipality);
        
        List<AccountOrder> result = new ArrayList<>();

        for (String orderType : orderTypes) {
	        String query = "/account/KSPCICS/pending?type=" + orderType;
	
	        try {
	            ResponseEntity<AccountOrderResponse> response = restTemplate.exchange(url + query, HttpMethod.GET, request, AccountOrderResponse.class);
	            if (response.getStatusCodeValue() != 200) {
	            	log.warn("Could not fetch accountOrders for municipality: " + municipality.getName() + ". HTTP " + response.getStatusCodeValue());

	            	return null;
	            }

	            result.addAll(response.getBody().getPendingOrders());
	        }
	        catch (HttpStatusCodeException ex) {
            	log.warn("Could not fetch accountOrders for municipality: " + municipality.getName() + ". HTTP " + ex.getRawStatusCode());
	
	            return null;
	        }
        }
        
        return result;
    }

    @Cacheable(value = "OrgUnitLosId")
    public String getOrgUnitLOSId(Municipality municipality, String uuid) {
        HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
        String url = getUrl(municipality);
        String query = "/v2/orgUnits/" + uuid;

        try {
            ResponseEntity<OrgUnit> response = restTemplate.exchange(url + query, HttpMethod.GET, request, OrgUnit.class);
            if (response.getStatusCodeValue() != 200) {
            	log.warn("Could not find OrgUnit with uuid " + uuid);

            	return null;
            }

            return xmlEncode(response.getBody().getShortname());
        }
        catch (HttpStatusCodeException ex) {
            log.warn(municipality.getName() + ": Failed to fetch OrgUnit. " + ex.getRawStatusCode() + ", response=" + ex.getResponseBodyAsString());

            return null;
        }
    }

	@Caching(evict = {
			@CacheEvict(value = "OrgUnitLosId", allEntries = true)
			})
	public void clearCache() {

	}

	// everyday at noon
	@Scheduled(cron = "0 0 12 * * ?")
	public void resetCache() {
		self.clearCache();
	}
	
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
				throw new Exception("Failed to fetch a list of Persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			
			empty = (response.getBody().getPersons() == null || response.getBody().getPersons().size() == 0);
			if (!empty) {
				result.addAll(response.getBody().getPersons());
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
			if (ex.getRawStatusCode() != 404) {
				log.error("Failed to fetch person. " + ex.getRawStatusCode());
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
			if (ex.getRawStatusCode() != 404) {
				throw new Exception("Failed to fetch person. " + ex.getRawStatusCode());
			}
		}

		return new ArrayList<Person>();
    }

	public void update(Person person, Municipality municipality) throws Exception {
        log.info(municipality.getName() + ": Patching person. uuid={}", person.getUuid());

        // have to do this, so we do not mess with existing affiliations, but we need
        // to read them, to know what LOS ID to set on the CICS account when creating
        person.setAffiliations(null);
        
        HttpEntity<Person> request = new HttpEntity<>(person, getHeaders(municipality));

		ResponseEntity<String> response = restTemplate.exchange(getUrl(municipality) + "/v2/persons/" + person.getUuid(), HttpMethod.PATCH, request, String.class);
		if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 399) {
			log.error(municipality.getName() + ": Failed to update Person " + person.getUuid() + ". " + response.getStatusCodeValue());
		}
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
    
    // expand as needed
	private String xmlEncode(String shortname) {
		return shortname.replace("&", "&amp;");
	}
}
