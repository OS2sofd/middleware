package dk.digitalidentity.brugertjek.integration.rc;

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
class RoleCatalogService {
	private RestClient restClient;
	private final BrugertjekConfiguration configuration;
	
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.restClient = RestClient.builder()
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

	RoleCatalogueRolesResponse lookupRoles(Municipality municipality, String userId) {
		RoleCatalogueRolesResponse response = null;
		
		if (configuration.getDevMode().isRcDevMode()) {
			// TODO: better test-data
		}
		else if (!municipality.isRolecatalogEnabled()) {
			log.warn(municipality.getName() + " : lookupRoles called - but RC not enabled");
		}
		else {
			try {
				ResponseEntity<RoleCatalogueRolesResponse> httpResponse = restClient.get()
		            .uri(municipality.getRolecatalogUrl() + "/api/user/" + userId + "/rolesAsList?system=")
		            .header("ApiKey", municipality.getRolecatalogApikey())
		            .retrieve()
		            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
		            	log.warn(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
		            	log.error(municipality.getName() + " : Got HTTP " + res.getStatusCode().value() + " on lookup for " + userId);
		            })
		            .toEntity(RoleCatalogueRolesResponse.class);
				
				if (httpResponse.getStatusCode().is2xxSuccessful()) {
					response = httpResponse.getBody();
				}

				if (response != null) {
					log.info(municipality.getName() + " : Successfully fetched roles for: " + userId);
				}
			}
			catch (Exception ex) {
				log.warn(municipality.getName() + " : Failed to connect to OS2rollekatalog", ex);
			}
		}
		
		return response;
	}
}
