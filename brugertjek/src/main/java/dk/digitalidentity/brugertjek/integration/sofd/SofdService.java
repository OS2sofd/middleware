package dk.digitalidentity.brugertjek.integration.sofd;

import java.util.UUID;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dk.digitalidentity.brugertjek.config.BrugertjekConfiguration;
import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableCaching
@RequiredArgsConstructor
class SofdService {
	private RestClient restClient;
	private final BrugertjekConfiguration configuration;
	
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.restClient = RestClient.builder()
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Cacheable(value = "orgUnitNames")
    String getOrgUnitName(Municipality municipality, String uuid) {
		String orgUnitName = null;
		
		if (configuration.getDevMode().isSofdDevMode()) {
			return "Dummy Enhed";
		}
		else if (!municipality.isOS2sofdEnabled()) {
			log.warn(municipality.getName() + " : getOrgUnitName called - but OS2sofd not enabled");
		}
		else {
			try {
				ResponseEntity<OrgUnit> result = restClient.get()
		            .uri(municipality.getSofdUrl() + "/api/v2/orgUnits/" + uuid)
		            .header("ApiKey", municipality.getSofdApikey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for OU " + uuid);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for OU " + uuid);
		            })
		            .toEntity(OrgUnit.class);

				if (result.getStatusCode().is2xxSuccessful()) {
					orgUnitName = result.getBody().getName();
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2sofd", ex);
			}
		}
		
		return orgUnitName;
    }
    
    @CacheEvict(value = "orgUnitNames", allEntries = true)
    void clearCache() {
    	;
    }
    
	Person getPersonByUserId(Municipality municipality, String userId) {
		Person person = null;
		
		if (configuration.getDevMode().isSofdDevMode()) {
			return dummyPerson(municipality, userId, null);
		}
		else if (!municipality.isOS2sofdEnabled()) {
			log.warn(municipality.getName() + " : getPersonByUserId called - but OS2sofd not enabled");
		}
		else {
			try {
				ResponseEntity<Person> result = restClient.get()
		            .uri(municipality.getSofdUrl() + "/api/v2/persons/byADUserId/" + userId)
		            .header("ApiKey", municipality.getSofdApikey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .toEntity(Person.class);

				if (result.getStatusCode().is2xxSuccessful()) {
					person = result.getBody();
				}

				if (person != null) {
					log.info(municipality.getName() + " : Successfully fetched person: " + userId);
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2sofd", ex);
			}
		}
		
		return person;
	}

	Person getPersonByKombitUuid(Municipality municipality, String kombitUuid) {
		Person person = null;
		
		if (configuration.getDevMode().isSofdDevMode()) {
			return dummyPerson(municipality, null, kombitUuid);
		}
		else if (!municipality.isOS2sofdEnabled()) {
			log.warn(municipality.getName() + " : getPersonByKombitUuid called - but OS2sofd not enabled");
		}
		else {
			try {
				ResponseEntity<Person> result = restClient.get()
		            .uri(municipality.getSofdUrl() + "/api/v2/persons/byKombitUuid/" + kombitUuid)
		            .header("ApiKey", municipality.getSofdApikey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + kombitUuid);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + kombitUuid);
		            })
		            .toEntity(Person.class);

				if (result.getStatusCode().is2xxSuccessful()) {
					person = result.getBody();
				}

				if (person != null) {
					log.info(municipality.getName() + " : Successfully fetched person: " + kombitUuid);
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2sofd", ex);
			}
		}
		
		return person;
	}
	
	private Person dummyPerson(Municipality municipality, String userId, String kombitUuid) {
		SofdUser sofdUser = new SofdUser();
		sofdUser.setDisabled(false);
		sofdUser.setKombitUuid((kombitUuid != null) ? kombitUuid : UUID.randomUUID().toString());
		sofdUser.setPrime(true);
		sofdUser.setUserId((userId != null) ? userId : "testtest");
		sofdUser.setUserType("ACTIVE_DIRECTORY");
		sofdUser.setUuid(UUID.randomUUID().toString());
		
		Person person = new Person();
		person.setCpr("0101010101");
		person.setFirstname("Test");
		person.setSurname("Testesen");			
		person.getUsers().add(sofdUser);

		log.info(municipality.getName() + " : DEVMODE: Successfully fetched person: " + userId);
		
		return person;
	}
}