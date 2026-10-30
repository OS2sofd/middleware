package dk.digitalidentity.brugertjek.integration.os2faktor;

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
public class OS2faktorValidationService {
	private final String OS2FAKTOR_VALIDATION_LABEL = "OS2faktor";
	private final OS2faktorService os2faktorService;

	public CoreDataExtendedLookup getStatus(Municipality municipality, String userId) {
		try {
			return os2faktorService.getStatusByUserId(municipality, userId);
		}
		catch (Exception ex) {
			log.warn("Could not connect to OS2faktor", ex);

			return null;
		}
	}
	
	public List<Validation> validate(Municipality municipality, String userId, CoreDataExtendedLookup lookup) {
		if (lookup == null) {
			return Collections.singletonList(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev ikke fundet"));
		}

		List<Validation> validations = new ArrayList<>();

		if (lookup.getMitIdErhvervStatus() != null && !"Active".equals(lookup.getMitIdErhvervStatus())) {
			validations.add(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er inaktiv i MitID Erhverv"));
		}

		if (lookup.isOs2faktorLocked()) {
			validations.add(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er spærret i OS2faktor"));
		}

		if (lookup.isOs2faktorBadPassword()) {
			validations.add(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " har dårlig kodeords-status i OS2faktor"));
		}

		if (lookup.isOs2faktorPasswordAdOutOfSync()) {
			validations.add(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_WARNING, userId + " har forskellige kodeord i OS2faktor og AD"));
		}

		// all is okay :)
		if (validations.size() == 0) {
			validations.add(new Validation(OS2FAKTOR_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
		}

		return validations;
	}
}
