package dk.digitalidentity.brugertjek.integration.nexus;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import dk.digitalidentity.brugertjek.config.BrugertjekConfiguration;
import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
class NexusService {
	private RestClient restClient;
	private final BrugertjekConfiguration configuration;
	
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.restClient = RestClient.builder()
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

	EmployeeDTO getStatusByUserId(Municipality municipality, String userId) {
		EmployeeDTO employee = null;

		if (configuration.getDevMode().isNexusDevMode()) {
			return dummyLookup(municipality, userId);
		}
		else if (!municipality.isNexusEnabled() || !StringUtils.hasText(configuration.getNexus().getApiKey())) {
			log.warn(municipality.getName() + " : getStatusByUserId called - but KMD Nexus not enabled");
		}
		else {
			try {
				ResponseEntity<EmployeeDTO> result = restClient.get()
		            .uri(configuration.getNexus().getUrl() + "/api/lookup/" + userId + "/" + municipality.getCvr())
		            .header("ApiKey", configuration.getNexus().getApiKey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .toEntity(EmployeeDTO.class);

				if (result.getStatusCode().is2xxSuccessful()) {
					employee = result.getBody();
				}

				if (employee != null) {
					log.info(municipality.getName() + " : Successfully fetched status for: " + userId);
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to NexusSync", ex);
			}
		}
		
		return employee;
	}
	
	private EmployeeDTO dummyLookup(Municipality municipality, String userId) {
		EmployeeDTO employee = new EmployeeDTO();
		employee.setActive(false);
		employee.setNexusId(512);
		employee.setNexusSyncStatus("Synkroniseret");
		employee.setIdentityId("user@kommune.dk");

		log.info(municipality.getName() + " : DEVMODE: Successfully fetched KMD Nexus status: " + userId);
		
		return employee;
	}
}