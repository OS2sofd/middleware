package dk.digitalidentity.service.fkorg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.fkorg.model.FKOrgResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FKOrgService {
	// TODO: move into configuration to make it easier to use while developing
	private String url = "http://os2sync.digital-identity.dk";
	private int errorCounter = 0;

	@Autowired
	private RestTemplate restTemplate;

	public boolean isPersonCreated(Municipality municipality, String uuid) {
		try {
			HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getCvr()));
			ResponseEntity<FKOrgResponse> response = restTemplate.exchange(url + "/api/rawBruger/" + uuid, HttpMethod.GET, request, FKOrgResponse.class);
			
			if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
				errorCounter = 0;
				return response.getBody().getLaesOutput().getStandardRetur().getStatusKode().equals("20");
			}
			
			log.error("Failed to fetch person with uuid " + uuid + " from FKOrg. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
		catch (HttpStatusCodeException ex) {
			errorCounter++;
			
			if (errorCounter >= 10) {
				log.error("Failed contact FK Organisation", ex);
			}
			else {
				log.warn("Failed contact FK Organisation. HTTP " + ex.getRawStatusCode() + ", message: " + ex.getResponseBodyAsString());
			}
		}
		
		return false;
	}

	private HttpHeaders getHeaders(String cvr) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cvr", cvr);

		return headers;
	}
}
