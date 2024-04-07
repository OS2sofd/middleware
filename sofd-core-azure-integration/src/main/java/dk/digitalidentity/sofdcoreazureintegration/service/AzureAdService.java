package dk.digitalidentity.sofdcoreazureintegration.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hc.client5.http.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.NotAcceptableStatusException;

import dk.digitalidentity.sofdcoreazureintegration.config.SofdCoreAzureIntegrationConfiguration;
import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;
import dk.digitalidentity.sofdcoreazureintegration.security.BearerToken;
import dk.digitalidentity.sofdcoreazureintegration.security.TokenService;
import dk.digitalidentity.sofdcoreazureintegration.service.coredata.CoreDataEntry;
import dk.digitalidentity.sofdcoreazureintegration.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AzureAdService {

	@Autowired
	private SofdCoreAzureIntegrationConfiguration configuration;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private SyncService syncService;

	@Autowired
	private TokenService tokenService;

	public void fullSync(Municipality municipality) throws Exception {
		String url = configuration.getAzureAd().getBaseUrl() + configuration.getAzureAd().getApiVersion() + "/users/delta?" + getFields(municipality);
		
		BearerToken token = tokenService.getToken(municipality);
		municipality.setToken(token);
		
		// Fetch all entries and set deltaLinkUrl For delta sync
		List<CoreDataEntry> coreData = getCoreDataAndSetDeltaLink(url, municipality);

		if (coreData != null && !coreData.isEmpty()) {
			syncService.fullSyncMunicipality(municipality, coreData);
		}
	}

	public void deltaSync(Municipality municipality) throws Exception {
		if (!StringUtils.hasLength(municipality.getDeltaLink())) {
			log.debug("Skipping DeltaSync since no deltaLink was saved");
			return;
		}
		
		BearerToken token = tokenService.getToken(municipality);
		municipality.setToken(token);

		// Fetch all entries and set next deltaLinkUrl
		List<CoreDataEntry> coreData = getCoreDataAndSetDeltaLink(municipality.getDeltaLink(), municipality);
		
		if (coreData != null && !coreData.isEmpty()) {
			syncService.deltaSyncMunicipality(municipality, coreData);
		}
	}

	@SuppressWarnings("unchecked")
	private List<CoreDataEntry> getCoreDataAndSetDeltaLink(String url, Municipality municipality) throws Exception {
		List<CoreDataEntry> coreDataEntries = new ArrayList<>();
		boolean morePages = true;

		while (morePages) {
			Map<String, Object> responseBody = getFromAzureAd(url, municipality.getToken());

			if (!responseBody.containsKey("value")) {
				LinkedHashMap<String, String> error = (LinkedHashMap<String, String>) responseBody.get("error");
				String message = "An error occurred while getting core data";
				if (error != null && error.containsKey("message")) {
					message = error.get("message");
				}

				throw new Exception(message);
			}

			coreDataEntries.addAll(convertHashMapToCoreDataFormat((ArrayList<LinkedHashMap<String, String>>) responseBody.get("value"), municipality));
			
			if (responseBody.containsKey("@odata.nextLink")) {
				url = (String) responseBody.get("@odata.nextLink");
			}
			else if (responseBody.containsKey("@odata.deltaLink")) {
				// All entries has been fetched, save deltaLink for next deltaSave
				municipality.setDeltaLink((String) responseBody.get("@odata.deltaLink"));
				morePages = false;
			}
			else {
				throw new Exception("No Delta or next link in response from AzureAD");
			}
		}

		return coreDataEntries;
	}

	private Map<String, Object> getFromAzureAd(String url, BearerToken token) throws HttpResponseException, NotAcceptableStatusException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(Constants.Authorization, "Bearer " + token.getAccessToken());
		headers.set(Constants.MaxPageSize, "1000");

		HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() { });

		if (response.getStatusCode().isError()) {
			throw new HttpResponseException(response.getStatusCode().value(), "Response from AzureAD was not 2xx.");
		}

		// Handle error and extract message if present
		Map<String, Object> responseBody = response.getBody();
		if (responseBody == null) {
			throw new NotAcceptableStatusException("Response body from AzureAD was null");
		}

		return responseBody;
	}

	private List<CoreDataEntry> convertHashMapToCoreDataFormat(ArrayList<LinkedHashMap<String, String>> userList, Municipality municipality) {
		List<CoreDataEntry> entries = new ArrayList<>();

		if (userList.isEmpty()) {
			return entries;
		}

		for (LinkedHashMap<String, String> userMap : userList) {
			CoreDataEntry entry = new CoreDataEntry();

			// Skip deleted entries, these will only be dealt with when doing a full sync
			if (userMap.containsKey("@removed")) {
				continue;
			}

			// Skip users with nothing in the CPR field (same as AD integration)
			String cpr = userMap.get(municipality.getCprField());
			if (!StringUtils.hasLength(cpr)) {
				continue;
			}

			String uuid = toUUID(userMap.get("onPremisesImmutableId"));
//			String uuid = userMap.get("id"); //works for debugging or Azure AD returns id for onPremisesImmutableId
			if (uuid == null) {
				log.warn("Skipping id=" + userMap.get("id") + " due to missing onPremisesImmutableId");
				continue;
			}
			
			// fix bad formatting
			cpr = cpr.trim().replace("-", "");
			if (cpr.length() != 10) {
				log.warn("User uuid=" + uuid + " has an invalid formatted CPR number");
				continue;
			}
			
			// Default fields
			entry.setAzureInternalId(userMap.get("id"));
			entry.setUuid(uuid);
			entry.setCpr(cpr);
			entry.setEmail(userMap.getOrDefault("mail", ""));
			entry.setUserId(userMap.getOrDefault(municipality.getUserIdField(), null));
			entry.setFirstName(userMap.get("givenName"));
			entry.setLastName(userMap.get("surname"));

			entries.add(entry);
		}

		return entries;
	}

	private String getFields(Municipality municipality) {
		StringBuilder sb = new StringBuilder("$select=");
		// Default fields
		sb.append("id,");
		sb.append(municipality.getCprField()).append(",");
		sb.append("displayName,");
		sb.append("givenName,");
		sb.append("surname,");
		sb.append("mail,");
		sb.append("onPremisesImmutableId,");
		sb.append(municipality.getUserIdField()).append(",");

		return sb.toString();
	}
	
	private static String toUUID(String base64Encoded) {
		try {
			byte[] binaryEncoding = Base64.getDecoder().decode(base64Encoded);
			ByteBuffer source = ByteBuffer.wrap(binaryEncoding);
			ByteBuffer target = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putInt(source.getInt()).putShort(source.getShort()).putShort(source.getShort()).order(ByteOrder.BIG_ENDIAN).putLong(source.getLong());
	
			target.rewind();
	
			return new UUID(target.getLong(), target.getLong()).toString();
		}
		catch (Exception ex) {
			log.warn("Failed to decode: " + base64Encoded, ex);
			return null;
		}
	}
}