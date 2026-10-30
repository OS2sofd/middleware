package dk.digitalidentity.brugertjek.integration.os2faktor;

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

@Service
@Slf4j
@RequiredArgsConstructor
class OS2faktorService {
	private RestClient restClient;
	private final BrugertjekConfiguration configuration;
	
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.restClient = RestClient.builder()
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

	CoreDataExtendedLookup getStatusByUserId(Municipality municipality, String userId) {
		CoreDataExtendedLookup lookup = null;

		if (configuration.getDevMode().isOs2faktorDevMode()) {
			return dummyLookup(municipality, userId);
		}
		else if (!municipality.isOS2faktorEnabled()) {
			log.warn(municipality.getName() + " : getStatusByUserId called - but OS2faktor not enabled");
		}
		else {
			try {
				ResponseEntity<CoreDataExtendedLookup> result = restClient.get()
		            .uri(municipality.getOs2faktorUrl() + "/api/coredata/nemlogin/lookup/" + userId + "?domain=" + municipality.getOs2faktorDomain())
		            .header("ApiKey", municipality.getOs2faktorApikey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .toEntity(CoreDataExtendedLookup.class);

				if (result.getStatusCode().is2xxSuccessful()) {
					lookup = result.getBody();
				}

				if (lookup != null) {
					log.info(municipality.getName() + " : Successfully fetched status for: " + userId);
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2faktor", ex);
			}
		}
		
		return lookup;
	}
	
	private CoreDataExtendedLookup dummyLookup(Municipality municipality, String userId) {
		CoreDataExtendedLookup lookup = new CoreDataExtendedLookup();
		lookup.setMitIdErhvervPrivateMitID(true);
		lookup.setMitIdErhvervQualifiedSignature(false);
		lookup.setMitIdErhvervRid("12345678");
		lookup.setMitIdErhvervStatus("Active");
		lookup.setMitIdErhvervUuid(UUID.randomUUID().toString());
		lookup.setOs2faktorActiveCorporateId(true);
		lookup.setOs2faktorBadPassword(false);
		lookup.setOs2faktorLocked(true);

		log.info(municipality.getName() + " : DEVMODE: Successfully fetched OS2faktor status: " + userId);
		
		return lookup;
	}
}