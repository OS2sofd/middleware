package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}