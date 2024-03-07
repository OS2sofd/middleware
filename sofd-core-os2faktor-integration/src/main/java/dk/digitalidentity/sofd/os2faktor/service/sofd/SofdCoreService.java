package dk.digitalidentity.sofd.os2faktor.service.sofd;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataNemLoginStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SofdCoreService {

	@Autowired
	private RestTemplate restTemplate;

	public List<SofdPerson> getPersons(Municipality municipality) {
		String url = municipality.getSofdUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}
		
		url += "api/sync/adgrid/allad";

		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", municipality.getSofdApiKey());
		headers.add("Content-Type", "application/json");

		HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException("Failed to call SOFD Core for " + municipality.getName() + ".\n" + response.getBody());
		}

		try {
			return new ObjectMapper().readValue(response.getBody(), new TypeReference<List<SofdPerson>>() { });
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException("Failed to deserialize SOFD Core response for " + municipality.getName(), ex);
		}
	}

	public void loadNemLoginStatus(Municipality municipality, CoreDataNemLoginStatus status) {
		String url = municipality.getSofdUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}
		
		url += "api/nemlogin";

		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", municipality.getSofdApiKey());
		headers.add("Content-Type", "application/json");

		HttpEntity<CoreDataNemLoginStatus> request = new HttpEntity<>(status, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		if (response.getStatusCodeValue() == 404) {
			log.warn("Failed to set nemlogin status in SOFD Core for " + municipality.getName() + ", no endpoint!");			
		}
		else if (response.getStatusCodeValue() != 200) {
			log.error("Failed to set nemlogin status in SOFD Core for " + municipality.getName() + ", body = " + response.getBody());
		}
	}
}
