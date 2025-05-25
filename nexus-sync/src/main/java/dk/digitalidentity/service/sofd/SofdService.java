package dk.digitalidentity.service.sofd;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.sofd.model.AuditWrapper;
import dk.digitalidentity.service.sofd.model.ChangeType;
import dk.digitalidentity.service.sofd.model.OrgUnit;
import dk.digitalidentity.service.sofd.model.OrgUnitList;
import dk.digitalidentity.service.sofd.model.Person;
import dk.digitalidentity.service.sofd.model.PersonList;
import dk.digitalidentity.service.sofd.model.SyncResult;
import lombok.extern.slf4j.Slf4j;

@EnableCaching
@EnableScheduling
@Slf4j
@Service
public class SofdService {
	private static final int PAGE_SIZE = 300;

	@Autowired
	private SofdService self;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MunicipalityService municipalityService;
	
	public Person getPersonByUserId(String userId, Municipality municipality) {
		if (!StringUtils.hasLength(municipality.getSofdBaseUrl()) || !StringUtils.hasLength(municipality.getSofdApiKey())) {
			log.warn("Can't fetch person with userId " + userId + " from sofd. BaseUrl or ApiKey is not configured.");
			return null;
		}
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = "/api/v2/persons/byADUserId/" + userId;

		ResponseEntity<Person> response = restTemplate.exchange(municipality.getSofdBaseUrl() + query, HttpMethod.GET, request, Person.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.warn("Can't fetch person with userId " + userId + " from sofd. Status " + response.getStatusCodeValue() + ", response=" + response.getBody());
			return null;
		}

		return response.getBody();
	}

	public Person getPersonByUuid(String uuid, Municipality municipality) {
		if (!StringUtils.hasLength(municipality.getSofdBaseUrl()) || !StringUtils.hasLength(municipality.getSofdApiKey())) {
			log.warn("Can't fetch person with uuid " + uuid + " from sofd. BaseUrl or ApiKey is not configured.");
			return null;
		}
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = "/api/v2/persons/" + uuid;

		ResponseEntity<Person> response = restTemplate.exchange(municipality.getSofdBaseUrl() + query, HttpMethod.GET, request, Person.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.warn("Can't fetch person with uuid " + uuid + " from sofd. Status " + response.getStatusCodeValue() + ", response=" + response.getBody());
			return null;
		}

		return response.getBody();
	}

	@Cacheable("ous")
	public List<OrgUnit> getOrgUnits(Municipality municipality) throws Exception {
		if (!StringUtils.hasLength(municipality.getSofdBaseUrl()) || !StringUtils.hasLength(municipality.getSofdApiKey())) {
			log.warn("Can't fetch orgUnits from sofd. BaseUrl or ApiKey is not configured.");
			return null;
		}

		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = "/api/v2/orgUnits?size=" + PAGE_SIZE;
		List<OrgUnit> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<OrgUnitList> response = restTemplate.exchange(municipality.getSofdBaseUrl() + query + "&page=" + page, HttpMethod.GET, request, OrgUnitList.class);
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

	private Long getHead(Municipality municipality) {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<Long> response = restTemplate.exchange(municipality.getSofdBaseUrl() + "/api/sync/head", HttpMethod.GET, request, Long.class);
		if (!response.getStatusCode().equals(HttpStatus.OK) || response.getBody() == null) {
			log.error("Failed to fetch sofd sync api head." + response.getStatusCodeValue() + ", response=" + response.getBody());
			return null;
		}

		return response.getBody();
	}

	private SyncResult getChangedPersons(Municipality municipality, long deltaOffset) {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<SyncResult> response = restTemplate.exchange(municipality.getSofdBaseUrl() + "/api/sync/persons?offset=" + deltaOffset, HttpMethod.GET, request, SyncResult.class);
		if (!response.getStatusCode().equals(HttpStatus.OK) || response.getBody() == null) {
			log.error("Failed to fetch changed persons from sofd. " + response.getStatusCodeValue() + ", response=" + response.getBody());
			return null;
		}

		return response.getBody();
	}

	public List<Person> getPersons(Municipality municipality) {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		String query = "/api/v2/persons?size=" + 1000;
		List<Person> result = new ArrayList<>();

		long page = 0;
		boolean empty = false;

		do {
			ResponseEntity<PersonList> response = restTemplate.exchange(municipality.getSofdBaseUrl() + query + "&page=" + page, HttpMethod.GET, request, PersonList.class);
			if (!response.getStatusCode().equals(HttpStatus.OK) || response.getBody() == null) {
				log.error("Failed to fetch a list of Persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
				return null;
			}

			result.addAll(response.getBody().getPersons());

			empty = (response.getBody().getPersons().size() == 0);
			page += 1;
		}
		while (!empty);

		return result;
	}

	public List<Person> getChangedPersons(Municipality municipality) {
		List<Person> result = new ArrayList<>();
		if (!StringUtils.hasLength(municipality.getSofdBaseUrl()) || !StringUtils.hasLength(municipality.getSofdApiKey())) {
			log.warn("Can't fetch changed persons from sofd. BaseUrl or ApiKey is not configured.");
			return result;
		}

		// always start by finding out what the current head is
		Long head = getHead(municipality);
		
		// if no offset is set it returns 0, and then we just update to latest head and do nothing (on this run)
		Long offset = municipality.getSofdSyncHead();
		if (offset == 0) {
			if (head == null) {
				return result;
			}
			else {
				municipality.setSofdSyncHead(head);
				municipalityService.save(municipality);

				return result;
			}
		}
		
		SyncResult syncResult = getChangedPersons(municipality, offset);
		if (syncResult == null) {
			return result;
		}
		
		List<String> uuids = syncResult.getUuids().stream().filter(p -> p.getChangeType().equals(ChangeType.UPDATE)).map(AuditWrapper::getUuid).toList();

		if (uuids.size() > 100) {
			log.info(municipality.getName() + " : Found " + uuids.size() + " changed persons, so performing a full sync");

			List<Person> allPeople = getPersons(municipality);

			if (allPeople != null) {
				result.addAll(allPeople.stream().filter(p -> uuids.contains(p.getUuid())).toList());
			}
		}
		else {
			for (String uuid : uuids) {
				Person person = getPersonByUuid(uuid, municipality);

				if (person != null) {
					result.add(person);
				}
			}
		}

		// set head so we skip these same changes next time
		if (head != null) {
			municipality.setSofdSyncHead(head);
			municipalityService.save(municipality);
		}
		
		log.info(municipality.getName() + " : Retrieved " + result.size() + " persons from SOFD for update");
		
		return result;
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");

		return headers;
	}

	@CacheEvict(value = "ous", allEntries = true)
	public void cleanUpOUs() {
	}

	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void cleanUpTask() {
		self.cleanUpOUs();
	}
}
