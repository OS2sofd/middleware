package dk.digitalidentity.brugertjek.integration.os2sync;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.boot.context.event.ApplicationReadyEvent;
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

@Slf4j
@Service
@RequiredArgsConstructor
class OS2SyncService {
	private RestClient restClient;
	private final BrugertjekConfiguration configuration;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.restClient = RestClient.builder()
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
	User getUser(Municipality municipality, String kombitUuid) throws Exception {
		User user = null;

		if (configuration.getDevMode().isOs2syncDevMode()) {
			user = new User();
			user.setUuid(UUID.randomUUID().toString());
			user.setShortKey(kombitUuid);
			user.setUserId(kombitUuid);
			user.setPhoneNumber("+4511111111");
			user.setLandline("+4511111112");
			user.setEmail("email@example.com");
			user.setStatus(1);
			user.setPositions(new ArrayList<>());
			Position pos1 = new Position();
			pos1.setName("Udviklingskonsulent");
			pos1.setOrgUnitUuid(UUID.randomUUID().toString());
			pos1.setStartDate(LocalDate.parse("2025-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			pos1.setStopDate(LocalDate.parse("2025-12-31", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			user.getPositions().add(pos1);
			Person person1 = new Person();
			person1.setName("Tester Userssen");
			person1.setCpr("1504876514");
			user.setPerson(person1);
	
			log.info(municipality.getName() + " : DEVMODE: Successfully fetched User: " + user.getUserId());
		}
		else {
			try {
				ResponseEntity<User> response = restClient.get()
		            .uri(configuration.getOs2sync().getUrl() + "/api/user/" + kombitUuid)
		            .header("Cvr", municipality.getCvr())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + kombitUuid);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + kombitUuid);
		            })
		            .toEntity(User.class);

				if (response.getStatusCode().is2xxSuccessful()) {
					user = response.getBody();
				}

				if (user != null) {
					log.info(municipality.getName() + " : Successfully fetched User: " + user.getUserId());
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2sync", ex);
			}
		}

		return user;
	}
}
