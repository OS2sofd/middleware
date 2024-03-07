package dk.sofd.organization.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.sofd.organization.core.model.OrgUnit;
import dk.sofd.organization.core.model.Person;
import dk.sofd.organization.core.model.SOFDSubstituteAssignment;
import dk.sofd.organization.core.model.SyncResult;
import dk.sofd.organization.dao.model.Municipality;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoreService {

	@Autowired
	private RestTemplate restTemplate;

	public Person[] getPersons(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		
		String url = municipality.getSofdUrl() + "/sync/adgrid/persons";
		if (municipality.isIncludeSchoolADUsers())
		{
			if( municipality.getSchoolDomain() == null )
			{
				throw new Exception("Municipality (" + municipality.getName() + ") was set to include school users but no school domain was specified.");
			}
			url += "?includeSchoolADUsers=true";
		}
		else if (municipality.isIncludeUniloginUsers()) {
			url += "?includeUniloginUsers=true";
		}

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(response.getBody(), Person[].class);
	}
	
	public Person[] getAllAD(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));
		
		String url = municipality.getSofdUrl() + "/sync/adgrid/allad";

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of ad users. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(response.getBody(), Person[].class);
	}

	public SyncResult getDeltaPersons(Municipality municipality, long deltaOffset) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<String> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/persons?offset=" + deltaOffset, HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of persons. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(response.getBody(), SyncResult.class);
	}
	
	public OrgUnit[] getOrgUnits(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<String> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/adgrid/orgunits", HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of orgunits. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(response.getBody(), OrgUnit[].class);
	}

	public Long getHead(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		ResponseEntity<Long> response = restTemplate.exchange(municipality.getSofdUrl() + "/sync/head", HttpMethod.GET, request, Long.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch the head." + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return response.getBody();
	}

	public SOFDSubstituteAssignment[] getSubstituteContextAssignments(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getSofdApiKey()));

		if (log.isDebugEnabled()) {
			log.debug(municipality.getSofdUrl() + "/substitutes/assignments");
			log.debug("Request:\r\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
		}
		
		ResponseEntity<SOFDSubstituteAssignment[]> response = restTemplate.exchange(municipality.getSofdUrl() + "/substitutes/assignments", HttpMethod.GET, request, SOFDSubstituteAssignment[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch a list of substituteAssginments. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Response:\r\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody()));
		}
		
		return response.getBody();
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");
		headers.add("ClientVersion", "1.0.0");
		headers.add("Accept", "application/json");

		return headers;
	}
}
