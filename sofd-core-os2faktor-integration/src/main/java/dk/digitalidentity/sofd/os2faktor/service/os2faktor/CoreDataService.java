package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoreDataService {

	@Autowired
	private RestTemplate restTemplate;

	public boolean sendData(Municipality municipality, CoreData coreData) {
		String url = municipality.getOs2faktorUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}
		url += "api/coredata/full";

		HttpHeaders headers = new HttpHeaders();
		headers.set("apiKey", municipality.getOs2faktorApiKey());
		HttpEntity<CoreData> request = new HttpEntity<>(coreData, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		if (!HttpStatus.OK.equals(response.getStatusCode())) {
			String message = response.getBody() != null ? response.getBody() : "<empty>";
			HttpStatus statusCode = response.getStatusCode();
			log.error("Error submitting CoreData entry for " + municipality.getName() + ". Code: " + statusCode + ", Body: " + message);

			return false;
		}

		return true;
	}

	public CoreDataNemLoginStatus getNemLoginStatus(Municipality municipality) throws JsonMappingException, JsonProcessingException {
		String url = municipality.getOs2faktorUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}
		url += "api/coredata/nemloginStatus?domain=" + municipality.getOs2faktorDomain();

		HttpHeaders headers = new HttpHeaders();
		headers.set("apiKey", municipality.getOs2faktorApiKey());
		HttpEntity<?> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		
		if (response.getStatusCodeValue() == 200) {
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(response.getBody(), CoreDataNemLoginStatus.class);
		}
		else if (response.getStatusCodeValue() == 404) {
			log.warn(municipality.getName() + ": No endpoint available for nemlogin status");
		}
		else {		
			log.error("Error getting nemlogin status for " + municipality.getName() + ". Code: " + response.getStatusCodeValue() + ", Body: " + response.getBody());
		}

		return null;
	}
}