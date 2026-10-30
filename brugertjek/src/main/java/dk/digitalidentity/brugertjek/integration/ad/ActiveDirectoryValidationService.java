package dk.digitalidentity.brugertjek.integration.ad;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.util.Constants;
import dk.digitalidentity.framework.ad.config.ActiveDirectoryConfig;
import dk.digitalidentity.framework.ad.service.ActiveDirectoryService;
import dk.digitalidentity.framework.ad.service.model.GetAllAttributesRequest;
import dk.digitalidentity.framework.ad.service.model.GetAllAttributesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveDirectoryValidationService {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
    private static int ACCOUNTDISABLE = 0x0002;
    private static int LOCKOUT = 0x0010;

	private final String AD_VALIDATION_LABEL = "Active Directory";
	private final ActiveDirectoryService activeDirectoryService;

	// bit if a hack to switch between v1/v3 of AD URL
	private final ActiveDirectoryConfig configuration;

	// returns NULL if not possible to connect to AD
	// TODO: VERY VERY BAD that this is synchronized, remove it once everyone has moved to v3
	public synchronized Map<String, String> getAttributes(Municipality municipality, String userId) {
		if (!municipality.isAdEnabled()) {
			log.warn(municipality.getName() + " AD called but not enabled!");
			return new HashMap<String, String>();
		}

		try {
			GetAllAttributesRequest request = new GetAllAttributesRequest();
			request.setUserId(userId);
			request.setCvr(municipality.getCvr());
			request.setApiKey(municipality.getAdApikey());

			if (municipality.isAdV3()) {
				configuration.setUrl("https://ad-connect3.internal-dns.dk/");
			}
			else {
				configuration.setUrl("https://ad-connect.digital-identity.dk/");
			}

			GetAllAttributesResponse response = activeDirectoryService.getAllAttributes(request);

			if (response != null && response.isSuccess() && response.getAttributes() != null) {
				Map<String, String> attributes = response.getAttributes();
				
				log.info(municipality.getName() + " : Successfully fetched User: " + userId);

				// sort by key
				return attributes.entrySet()
				    .stream()
				    .sorted(Map.Entry.comparingByKey())
				    .collect(Collectors.toMap(
				        Map.Entry::getKey,
				        Map.Entry::getValue,
				        (e1, e2) -> e1,
				        LinkedHashMap<String, String>::new
				    ));
			}
			
			if (response != null && !response.isSuccess() && response.getMessage() != null && response.getMessage().contains("No authenticated WebSocket connection available")) {
				log.warn(municipality.getName() + " : Could not connect to ActiveDirectory: " + response.getMessage());

				return null;
			}
			
			return new HashMap<String, String>();
		}
		catch (Exception ex) {
			log.warn(municipality.getName() + " : Could not connect to ActiveDirectory", ex);

			return null;
		}
	}

	public List<Validation> validate(Municipality municipality, String userId, Map<String, String> attributes) {
		if (attributes == null) {
			return Collections.singletonList(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_ERROR, "Ingen forbindelse!"));			
		}

		if (attributes.size() == 0) {
			return Collections.singletonList(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev ikke fundet"));
		}

		List<Validation> validations = new ArrayList<>();

		if (attributes.containsKey("accountExpires")) {
			String adTts = attributes.get("accountExpires");

			if (!"Aldrig".equals(adTts)) {
				try {
					LocalDateTime dt = LocalDateTime.parse(adTts, formatter);
					
					if (dt.isBefore(LocalDateTime.now())) {
						validations.add(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er udløbet"));
					}
					else if (dt.isBefore(LocalDateTime.now().plusDays(7))) {
						validations.add(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_WARNING, userId + " udløber snart"));
					}
				}
				catch (Exception ex) {
					log.warn("Unable to parse timestamp: " + adTts);
				}
			}
		}

		if (attributes.containsKey("userAccountControl")) {
			String uac = attributes.get("userAccountControl");
			
			try {
				int userAccountControl = Integer.parseInt(uac);
				
				if ((userAccountControl & ACCOUNTDISABLE) != 0) {
					validations.add(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er disabled"));
				}
				
				if ((userAccountControl & LOCKOUT) != 0) {
					validations.add(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_WARNING, userId + " er låst ude"));
				}
				
			}
			catch (Exception ex) {
				log.warn("Unable to parse userAccountControl: " + uac);
			}
		}

		// all is okay :)
		if (validations.size() == 0) {
			validations.add(new Validation(AD_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
		}

		return validations;
	}
}
