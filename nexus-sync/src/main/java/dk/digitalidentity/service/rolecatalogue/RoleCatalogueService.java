package dk.digitalidentity.service.rolecatalogue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.WebClientBuilderService;
import dk.digitalidentity.service.nexus.model.Employee;
import dk.digitalidentity.service.rolecatalogue.model.ItSystem;
import dk.digitalidentity.service.rolecatalogue.model.UserRole;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoleCatalogueService {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebClientBuilderService webClientBuilderService;

    public List<UserRole> getUserRolesFromItSystem(Municipality municipality, String itSystemIdentifier) {
        try {
            WebClient client = webClientBuilderService.getRoleCatalogueWebClient(municipality);
            String url = municipality.getRoleCatalogueBaseUrl() + "/api/read/itsystem/" + itSystemIdentifier + "?indirectRoles=true";

            String response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            UserRole[] userRoles = mapper.readValue(response, UserRole[].class);

            return Arrays.asList(userRoles);
        }
        catch (Exception ex) {
            log.error("Failed to fetch userRoles from Nexus itSystem for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId(), ex);
        }

        return null;
    }

    public boolean saveItSystem(ItSystem itSystem, Municipality municipality){
        WebClient client = webClientBuilderService.getRoleCatalogueWebClient(municipality);

        String url = municipality.getRoleCatalogueBaseUrl() + "/api/itsystem/manage/" + itSystem.getId();
        ResponseEntity<?> response = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(itSystem)
                .retrieve()
                .toBodilessEntity()
                .block();

        if (HttpStatus.OK.equals(response.getStatusCode())) {
            return true;
        }

        log.error("Failed to save itSystem with id " + itSystem.getId() + " for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId());
        return false;
    }

    public Set<String> getUserIdsWithRolesFromItSystem(Municipality municipality) {
        try {
            WebClient client = webClientBuilderService.getRoleCatalogueWebClient(municipality);
            String url = municipality.getRoleCatalogueBaseUrl() + "/api/itsystem/" + municipality.getRoleCatalogueKMDNexusItSystemId() + "/users";

            String response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String[] userIds = mapper.readValue(response, String[].class);

            return new HashSet<>(Arrays.asList(userIds));

        }
        catch (Exception e) {
        	// on timeouts, just return nul
        	if (e instanceof WebClientRequestException ex && ex.contains(ReadTimeoutException.class)) {
                log.warn("Failed to fetch userIds from KMD Nexus itSystem for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId(), e);
                return null;
        	}

            log.error("Failed to fetch userIds from KMD Nexus itSystem for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId(), e);
        }

        return new HashSet<>();
    }

	public void updateAssignmentsForItSystem(Municipality municipality, List<Employee> employeesWithTrust) {
        try {
        	Set<String> userIds = employeesWithTrust.stream().map(e -> e.getInitials()).collect(Collectors.toSet());
        	
            WebClient client = webClientBuilderService.getRoleCatalogueWebClient(municipality);
            
            String url = municipality.getRoleCatalogueBaseUrl() + "/api/overwriteAssignments/userrole/" + municipality.getRoleCatalogueTrustRoleId();

            ResponseEntity<?> response = client.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userIds)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                log.error("Failed to update all trust user role assignments for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId() + ". Message: " + response.getBody());
            }
        }
        catch (Exception ex) {
    		log.error("Failed to update all trust user role assignments for municipality " + municipality.getName() + ", cvr: " + municipality.getCvr(), ", id: " + municipality.getId(), ex);
        }
    }
}
