package dk.digitalidentity.brugertjek.integration.entra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntraValidationService {
	private final String ENTRA_VALIDATION_LABEL = "EntraID";

	private final EntraService entraService;

	public EntraUserLookup lookup(Municipality municipality, String userId) {
		try {
			return entraService.getStatus(municipality, userId);
		}
		catch (EntraException ex) {
			log.warn("Could not connect to EntraID", ex);

			return null;
		}
	}
	
	public List<Validation> validate(Municipality municipality, String userId, EntraUserLookup lookup) {
		if (lookup == null) {
			return Collections.singletonList(new Validation(ENTRA_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev ikke fundet"));
		}

		List<Validation> validations = new ArrayList<>();

		if (!lookup.isActive()) {
			validations.add(new Validation(ENTRA_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er inaktiv i Entra"));
		}

		if (lookup.getLicenses().size() == 0) {
			validations.add(new Validation(ENTRA_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " har ingen licenser"));
		}

		if (validations.size() == 0) {
			validations.add(new Validation(ENTRA_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
		}

		return validations;
	}
}
