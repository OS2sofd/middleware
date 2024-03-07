package dk.digitalidentity.sofd.cics.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.kmd.Envelope;
import dk.digitalidentity.sofd.cics.service.kmd.ResultWrapper;
import dk.digitalidentity.sofd.cics.service.kmd.SearchEntry;
import dk.digitalidentity.sofd.cics.service.kmd.SearchWrapper;
import dk.digitalidentity.sofd.cics.service.model.Affiliation;
import dk.digitalidentity.sofd.cics.service.model.KspUser;
import dk.digitalidentity.sofd.cics.service.model.KspUsersResponse;
import dk.digitalidentity.sofd.cics.service.model.Person;
import dk.digitalidentity.sofd.cics.service.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KspCicsService {
	private static final char[] chars = "qwertyuioplkjhgfdsazxcvbnm".toCharArray();
	private static final char[] digits = "0123456789".toCharArray();
	private static final SecureRandom random = new SecureRandom();

	private static final String SUCCESS_RESPONSE = "urn:oasis:names:tc:SPML:1:0#success";
	private static final String SOAP_ARG_LOSSHORTNAME = "{{LOS_SHORTNAME}}";
	private static final String SOAP_ARG_CICS_USERID = "{{CICS_USERID}}";
	private static final String SOAP_ARG_CPR = "{{CPR}}";
	private static final String SOAP_ARG_FIRSTNAME = "{{FIRSTNAME}}";
	private static final String SOAP_ARG_SURNAME = "{{SURNAME}}";
	private static final String SOAP_ARG_DEPARTMENT = "{{DEPARTMENT}}";
	private static final String SOAP_ARG_LOCAL_USERID = "{{LOCAL_USERID}}";
	private static final String SOAP_ARG_PASSWORD = "{{PASSWORD}}";
	
	// default wrappers around all requests
	private static final String SOAP_WRAPPER_BEGIN =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
    		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" + 
    		"                  xmlns:kmd=\"http://www.kmd.dk/KMD.YH.KSPAabenSpml\">" + 
    		"  <soapenv:Header/>" + 
    		"  <soapenv:Body>";

	private static final String SOAP_WRAPPER_END =
			"  </soapenv:Body>" + 
    		"</soapenv:Envelope>";

	// actual payload for searching for users
	private static final String SOAP_SEARCH_USERS =
			"<kmd:SPMLSearchRequest>" + 
		    "  <kmd:request><![CDATA[" + 
    		"  <spml:searchRequest xmlns:spml=\"urn:oasis:names:tc:SPML:1:0\" xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">" +
			"    <spml:searchBase type=\"urn:oasis:names:tc:SPML:1:0#UserIDAndOrDomainName\">" + 
    		"      <spml:id>" + SOAP_ARG_LOSSHORTNAME + "</spml:id>" + 
    		"    </spml:searchBase>" +
			"  </spml:searchRequest>" +
			"  ]]></kmd:request>" + 
			"</kmd:SPMLSearchRequest>";
	
	// actual payload for deleting a user
	private static final String SOAP_DELETE_USER =
			"<kmd:SPMLDeleteRequest>" + 
		    "  <kmd:request><![CDATA[" + 
			"  <spml:deleteRequest xmlns:spml=\"urn:oasis:names:tc:SPML:1:0\" xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">" + 
			"    <spml:identifier type=\"urn:oasis:names:tc:SPML:1:0#UserIDAndOrDomainName\">" + 
    		"      <spml:id>" + SOAP_ARG_CICS_USERID + "</spml:id>" + 
    		"    </spml:identifier>" +
    		"  </spml:deleteRequest>" + 
			"  ]]></kmd:request>" + 
			"</kmd:SPMLDeleteRequest>";
	
	// actual payload for creating a user
	private static final String SOAP_CREATE_USER =
			"<kmd:SPMLAddRequest>" + 
		    "  <kmd:request><![CDATA[" + 
			"  <spml:addRequest xmlns:spml=\"urn:oasis:names:tc:SPML:1:0\" xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">" + 
			"    <spml:attributes>" + 
			"      <dsml:attr name=\"objectclass\">" + 
			"        <dsml:value>urn:kmd:names:tc:SPML:KspUser</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"uid\">" + 
			"        <dsml:value>" + SOAP_ARG_CICS_USERID + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"PersonCivilRegistrationIdentifier\">" + 
			"        <dsml:value>" + SOAP_ARG_CPR + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"givenName\">" + 
			"        <dsml:value>" + SOAP_ARG_FIRSTNAME + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"sn\">" + 
			"        <dsml:value>" + SOAP_ARG_SURNAME + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"departmentNumber\">" + 
			"        <dsml:value>" + SOAP_ARG_DEPARTMENT + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"x500LocalUserId\">" + 
			"        <dsml:value>" + SOAP_ARG_LOCAL_USERID + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"      <dsml:attr name=\"userPassword\">" + 
			"        <dsml:value>" + SOAP_ARG_PASSWORD + "</dsml:value>" + 
			"      </dsml:attr>" + 
			"    </spml:attributes>" + 
			"  </spml:addRequest>" +
			"  ]]></kmd:request>" + 
			"</kmd:SPMLAddRequest>";

	// actual payload for moving a user
	private static final String SOAP_MOVE_USER =
			"<kmd:SPMLModifyRequest>" + 
		    "  <kmd:request><![CDATA[" + 
			"  <spml:modifyRequest xmlns:spml=\"urn:oasis:names:tc:SPML:1:0\" xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">" +
			"    <spml:identifier type=\"urn:oasis:names:tc:SPML:1:0#UserIDAndOrDomainName\">" + 
    		"      <spml:id>" + SOAP_ARG_CICS_USERID + "</spml:id>" + 
    		"    </spml:identifier>" +
			"    <spml:modifications>" + 
			"      <dsml:modification name=\"departmentNumber\" operation=\"replace\">" + 
			"        <dsml:value>" + SOAP_ARG_DEPARTMENT + "</dsml:value>" + 
			"      </dsml:modification>" +
			"    </spml:modifications>" + 
			"  </spml:modifyRequest>" +
			"  ]]></kmd:request>" + 
			"</kmd:SPMLModifyRequest>";

	@Autowired
	private RestTemplateFactory restTemplateFactory;
	
	@Autowired
	private SOFDService sofdService;

	@Value("${kmd.kspcics.url:https://int-ewswlbs-wm3q2021.kmd.dk/KMD.YH.KSPAabenSpml/KSPAabenSPML.asmx}")
	private String kspCicsUrl;
	
	public List<KspUser> loadAllCicsUsers(Municipality municipality) {
		KspUsersResponse response = findUsers(municipality);

		if (response != null && response.getUsers() != null && response.getUsers().size() > 0) {
			return response.getUsers();
		}
		
		return new ArrayList<>();
	}
	
	public String createUser(Municipality municipality, Person person, String userId) {
    	HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/xml; charset=utf-8");
        headers.add("SOAPAction", "http://www.kmd.dk/KMD.YH.KSPAabenSpml/SPMLAddRequest");

        // TODO: this is not how it should be done - but this works in Kalundborg.
        //       the problem is that we need the UPN, which just happens to match
        //       the EXCHANGE account, but that is not given
        Optional<User> user = person.getUsers().stream()
        		.filter(u -> u.getUserType().equals("EXCHANGE") && u.isPrime())
        		.findFirst();

		if (!user.isPresent()) {
			user = person.getUsers().stream().filter(u -> u.getUserType().equals("ACTIVE_DIRECTORY") && u.isPrime())
					.findFirst();

			if (!user.isPresent()) {
				return "Personen har ikke en EXCHANGE konto at knytte CICS kontoen til, og fallback til sAMAccountName var ikke muligt";
			}
		}
		
        String localUserId = user.get().getUserId();
        
        Optional<Affiliation> affiliation = person.getAffiliations().stream().filter(a -> a.isPrime()).findFirst();
        if (!affiliation.isPresent()) {
        	return "Personen har ikke et primært tilhørsforhold";
        }
        
        String department = sofdService.getOrgUnitLOSId(municipality, affiliation.get().getOrgUnitUuid());
        if (department == null) {
        	return "Personen er ikke tilknyttet en LOS enhed";
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(SOAP_WRAPPER_BEGIN);
        builder.append(SOAP_CREATE_USER.replace(SOAP_ARG_CICS_USERID, userId)
        							   .replace(SOAP_ARG_CPR, person.getCpr())
        							   .replace(SOAP_ARG_FIRSTNAME, person.getFirstname())
        							   .replace(SOAP_ARG_SURNAME, person.getSurname())
        							   .replace(SOAP_ARG_DEPARTMENT, department)
        							   .replace(SOAP_ARG_LOCAL_USERID, localUserId)
        							   .replace(SOAP_ARG_PASSWORD, randomPassword()));
        builder.append(SOAP_WRAPPER_END);

        String payload = builder.toString();
    	HttpEntity<String> request = new HttpEntity<String>(payload, headers);
		ResponseEntity<String> response;
		
		RestTemplate restTemplate = null;
		try {
			restTemplate = restTemplateFactory.getRestTemplate(municipality);
		}
		catch (Exception ex) {
			log.error("Failed to get a RestTemplate for " + municipality.getName(), ex);
			return "Failed to get a RestTemplate";
		}
		
    	// KMD has some issues, so we might have to try multiple times
    	int tries = 3;
    	do {
    		response = restTemplate.postForEntity(kspCicsUrl, request, String.class);
			if (response.getStatusCodeValue() != 200) {
				if (--tries >= 0) {
					log.warn(municipality.getName() + ": CreateUser - Got responseCode " + response.getStatusCodeValue() + " from service");
					
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException ex) {
						;
					}
				}
				else {
					log.error(municipality.getName() + ": CreateUser - Got responseCode " + response.getStatusCodeValue() + " from service. Request=" + request + " / Response=" + response.getBody());
					return ("KSP/CICS HTTP ErrorCode: " + response.getStatusCodeValue());
				}
			}
			else {
				break;
			}
    	} while (true);

		String responseBody = response.getBody();		
		ResultWrapper wrapper = null;

		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			Envelope envelope = xmlMapper.readValue(responseBody, Envelope.class);
			String addResponse = envelope.getBody().getSpmlAddRequestResponse().getSpmlAddRequestResult();
	
			wrapper = xmlMapper.readValue(addResponse, ResultWrapper.class);
		}
		catch (Exception ex) {
			log.error(municipality.getName() + ": CreateUser - Failed to decode response for " + municipality.getName() + ": " + responseBody, ex);
			
			return "Failed to decode response from KSP/CICS";
		}

		if (!SUCCESS_RESPONSE.equals(wrapper.getResult())) {
			log.error(municipality.getName() + ": CreateUser - Got a non-success response: " + wrapper.getResult() + " for " + municipality.getName() + ". Request=" + payload + " / Response=" + responseBody);
			
			return ("KSP/CICS ErrorCode: " + wrapper.getErrorMessage() + " (" + wrapper.getResult() + ")");
		}
		
		// null is success
		return null;
	}
	
	public String moveUser(Municipality municipality, Person person, String userId) {
    	HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/xml; charset=utf-8");
        headers.add("SOAPAction", "http://www.kmd.dk/KMD.YH.KSPAabenSpml/SPMLModifyRequest");

        Optional<Affiliation> affiliation = person.getAffiliations().stream().filter(a -> a.isPrime()).findFirst();
        if (!affiliation.isPresent()) {
        	return "Personen har ikke et primært tilhørsforhold";
        }

        String department = sofdService.getOrgUnitLOSId(municipality, affiliation.get().getOrgUnitUuid());
        if (department == null) {
        	return "Personen er ikke tilknyttet en LOS enhed";
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(SOAP_WRAPPER_BEGIN);
        builder.append(SOAP_MOVE_USER.replace(SOAP_ARG_CICS_USERID, userId)
        							 .replace(SOAP_ARG_DEPARTMENT, department));
        builder.append(SOAP_WRAPPER_END);

        String payload = builder.toString();
    	HttpEntity<String> request = new HttpEntity<String>(payload, headers);
		ResponseEntity<String> response;
		
		RestTemplate restTemplate = null;
		try {
			restTemplate = restTemplateFactory.getRestTemplate(municipality);
		}
		catch (Exception ex) {
			log.error("Failed to get a RestTemplate for " + municipality.getName(), ex);
			return "Failed to get a RestTemplate";
		}
		
    	// KMD has some issues, so we might have to try multiple times
    	int tries = 3;
    	do {
    		response = restTemplate.postForEntity(kspCicsUrl, request, String.class);
			if (response.getStatusCodeValue() != 200) {
				if (--tries >= 0) {
					log.warn("MoveUser - Got responseCode " + response.getStatusCodeValue());
					
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException ex) {
						;
					}
				}
				else {
					log.error("MoveUser - Got responseCode " + response.getStatusCodeValue() + " from service. Request=" + request + " / Response=" + response.getBody());
					return ("KSP/CICS HTTP ErrorCode: " + response.getStatusCodeValue());
				}
			}
			else {
				break;
			}
    	} while (true);

		String responseBody = response.getBody();		
		ResultWrapper wrapper = null;

		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			Envelope envelope = xmlMapper.readValue(responseBody, Envelope.class);
			String addResponse = envelope.getBody().getSpmlModifyRequestResponse().getSpmlModifyRequestResult();
	
			wrapper = xmlMapper.readValue(addResponse, ResultWrapper.class);
		}
		catch (Exception ex) {
			log.error("MoveUser - Failed to decode response for " + municipality.getName() + ": " + responseBody, ex);
			
			return "Failed to decode response from KSP/CICS";
		}

		if (!SUCCESS_RESPONSE.equals(wrapper.getResult())) {
			log.error("MoveUser - Got a non-success response: " + wrapper.getResult() + " for " + municipality.getName() + ". Request=" + payload + " / Response=" + responseBody);
			
			return ("KSP/CICS ErrorCode: " + wrapper.getErrorMessage() + " (" + wrapper.getResult() + ")");
		}
		
		// null is success
		return null;
	}
	
	public String deleteUser(Municipality municipality, String userId) {
    	HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/xml; charset=utf-8");
        headers.add("SOAPAction", "http://www.kmd.dk/KMD.YH.KSPAabenSpml/SPMLDeleteRequest");

        StringBuilder builder = new StringBuilder();
        builder.append(SOAP_WRAPPER_BEGIN);
        builder.append(SOAP_DELETE_USER.replace(SOAP_ARG_CICS_USERID, userId));
        builder.append(SOAP_WRAPPER_END);

        String payload = builder.toString();
    	HttpEntity<String> request = new HttpEntity<String>(payload, headers);
		ResponseEntity<String> response;
		
		RestTemplate restTemplate = null;
		try {
			restTemplate = restTemplateFactory.getRestTemplate(municipality);
		}
		catch (Exception ex) {
			log.error("Failed to get a RestTemplate for " + municipality.getName(), ex);
			return "Failed to get a RestTemplate";
		}
		
    	// KMD has some issues, so we might have to try multiple times
    	int tries = 3;
    	do {
    		response = restTemplate.postForEntity(kspCicsUrl, request, String.class);
			if (response.getStatusCodeValue() != 200) {
				if (--tries >= 0) {
					log.warn("DeleteUser - Got responseCode " + response.getStatusCodeValue() + " from service: " + response.getBody());
					
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException ex) {
						;
					}
				}
				else {
					log.error("DeleteUser - Got responseCode " + response.getStatusCodeValue() + " from service. Request=" + request + " / Response=" + response.getBody());
					return ("KSP/CICS HTTP ErrorCode: " + response.getStatusCodeValue());
				}
			}
			else {
				break;
			}
    	} while (true);

		String responseBody = response.getBody();		
		ResultWrapper wrapper = null;

		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			Envelope envelope = xmlMapper.readValue(responseBody, Envelope.class);
			String deleteResponse = envelope.getBody().getSpmlDeleteRequestResponse().getSpmlDeleteRequestResult();
	
			wrapper = xmlMapper.readValue(deleteResponse, ResultWrapper.class);
		}
		catch (Exception ex) {
			log.error("DeleteUser - Failed to decode response for " + municipality.getName() + ": " + responseBody, ex);
			
			return "Failed to decode response from KSP/CICS";
		}

		if (!SUCCESS_RESPONSE.equals(wrapper.getResult())) {
			if (wrapper.getErrorMessage().contains("Bruger findes ikke")) {
				log.warn("DeleteUser - Got a non-success response: " + wrapper.getResult() + " for " + municipality.getName() + ". Request=" + payload + " / Response=" + responseBody);
			}
			else {
				log.error("DeleteUser - Got a non-success response: " + wrapper.getResult() + " for " + municipality.getName() + ". Request=" + payload + " / Response=" + responseBody);
			}
			
			return ("KSP/CICS ErrorCode: " + wrapper.getErrorMessage() + " (" + wrapper.getResult() + ")");
		}
		
		// null is success
		return null;
	}

	private KspUsersResponse findUsers(Municipality municipality) {
    	HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/xml; charset=utf-8");
        headers.add("SOAPAction", "http://www.kmd.dk/KMD.YH.KSPAabenSpml/SPMLSearchRequest");

        StringBuilder builder = new StringBuilder();
        builder.append(SOAP_WRAPPER_BEGIN);
        builder.append(SOAP_SEARCH_USERS.replace(SOAP_ARG_LOSSHORTNAME, municipality.getCicsLosId()));
        builder.append(SOAP_WRAPPER_END);

        String payload = builder.toString();
    	HttpEntity<String> request = new HttpEntity<String>(payload, headers);
		ResponseEntity<String> response;
		
		RestTemplate restTemplate = null;
		try {
			restTemplate = restTemplateFactory.getRestTemplate(municipality);
		}
		catch (Exception ex) {
			log.error("Failed to get a RestTemplate for " + municipality.getName(), ex);
			return null;
		}
		
    	// KMD has some issues, so we might have to try multiple times
    	int tries = 3;
    	do {
    		response = restTemplate.postForEntity(kspCicsUrl, request, String.class);
			if (response.getStatusCodeValue() != 200) {
				if (--tries >= 0) {
					log.warn("FindUsers - Got responseCode " + response.getStatusCodeValue() + " from service: " + response.getBody());
					
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException ex) {
						;
					}
				}
				else {
					log.error("FindUsers - Got responseCode " + response.getStatusCodeValue() + " from service: " + response.getBody());
					return null;
				}
			}
			else {
				break;
			}
    	} while (true);

		String responseBody = response.getBody();		
		SearchWrapper wrapper = null;

		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			Envelope envelope = xmlMapper.readValue(responseBody, Envelope.class);
			String searchResponse = envelope.getBody().getSpmlSearchRequestResponse().getSpmlSearchRequestResult();
	
			wrapper = xmlMapper.readValue(searchResponse, SearchWrapper.class);
		}
		catch (Exception ex) {
			log.error("FindUsers - Failed to decode response for " + municipality.getName() + ": " + responseBody, ex);
			
			return null;
		}

		if (!SUCCESS_RESPONSE.equals(wrapper.getResult())) {
			log.error("FindUsers - Got a non-success response: " + wrapper.getResult() + " for " + municipality.getName() + ". Request: " + payload);
			
			return null;
		}
		
		KspUsersResponse kspUsersResponse = new KspUsersResponse();
		kspUsersResponse.setUsers(new ArrayList<>());
		for (SearchEntry entry : wrapper.getSearchResultEntry()) {
			KspUser kspUser = new KspUser();
			kspUser.setCpr(entry.getAttributes().getUserCpr());
			kspUser.setUserId(entry.getIdentifier().getId());
			kspUser.setDepartment(entry.getAttributes().getUserProfileDepartmentNumber());

			kspUsersResponse.getUsers().add(kspUser);
		}

		return kspUsersResponse;
	}
	
	private String randomPassword() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < 4; i++) {
			builder.append(chars[random.nextInt(chars.length)]);
		}
		
		for (int i = 0; i < 4; i++) {
			builder.append(digits[random.nextInt(digits.length)]);
		}
		
		return builder.toString();
	}
}
