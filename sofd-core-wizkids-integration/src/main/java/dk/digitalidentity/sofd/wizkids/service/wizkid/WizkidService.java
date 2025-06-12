package dk.digitalidentity.sofd.wizkids.service.wizkid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WizkidService {
	private static final int pageSize = 1000;

	@Autowired
	private RestTemplate restTemplate;

	public List<WizkidUser> getUsers(Municipality municipality) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality));
		String url = "https://dataapi.edulife.dk/api/integrations/employeesandteachers";

		List<WizkidUser> result = new ArrayList<>();
		boolean empty = false;
		long page = 1;

		do {
			ResponseEntity<WizkidResponse> response = restTemplate.exchange(url + "?pageNumber=" + page + "&pageSize=" + pageSize, HttpMethod.GET, request, WizkidResponse.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to fetch data from Wizkids. " + response.getStatusCode().value() + ", response=" + response.getBody());
			}

			result.addAll(response.getBody().getItems());
			
			log.info(municipality.getName() + " : found " + result.size() + " users");

			empty = (response.getBody().getItems().size() == 0);
			page += 1;
		} while (!empty);

		// trim the result - there are duplicates if they are in multiple registrations systems (e.g. TEA and KMD)
		List<WizkidUser> trimmedUsers = new ArrayList<>();
		Set<String> cprs = new HashSet<>();
		for (WizkidUser wuser : result) {
			if (!cprs.contains(wuser.getCivicNumber())) {
				cprs.add(wuser.getCivicNumber());
				trimmedUsers.add(wuser);
			}
		}
		
		return trimmedUsers;
	}

	private HttpHeaders getHeaders(Municipality municipality) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + municipality.getBearerToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return headers;
    }
}
