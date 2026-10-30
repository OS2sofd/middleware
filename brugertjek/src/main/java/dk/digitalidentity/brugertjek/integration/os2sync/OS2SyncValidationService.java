package dk.digitalidentity.brugertjek.integration.os2sync;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.sofd.Affiliation;
import dk.digitalidentity.brugertjek.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OS2SyncValidationService {
	private final String FK_ORG_VALIDATION_LABEL = "FK Organisation";
	private final OS2SyncService os2syncService;

	public User getUser(Municipality municipality, String kombitUuid) {
		try {
			return os2syncService.getUser(municipality, kombitUuid);
		}
		catch (Exception ex) {
			log.warn("Could not connect to FK Organisation", ex);

			return null;
		}
	}

	public List<Validation> validate(Municipality municipality, String kombitUuid, User user, List<Affiliation> affiliations) {
		if (user == null) {
			return Collections.singletonList(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_ERROR, "Der findes ingen bruger med UUID " + kombitUuid));
		}

		List<Validation> validations = new ArrayList<>();
		if (user.getStatus() != 1) {
			validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_ERROR, user.getUserId() + " er ikke aktiv"));
		}
		else {
			if (user.getPositions() == null || user.getPositions().size() == 0) {
				validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_WARNING, user.getUserId() + " er ikke ikke knyttet til nogen enheder"));
			}
			else {
				for (Position position : user.getPositions()) {
					if (position.getStopDate() != null) {
						if (position.getStopDate().isBefore(LocalDate.now())) {
							validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_WARNING, user.getUserId() + " har afsluttet relation til enhed"));
						}
						else if (position.getStopDate().isBefore(LocalDate.now().plusDays(7))) {
							validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_WARNING, user.getUserId() + " har relation til enhed der udløber indenfor 7 dage"));
						}
					}
				}
			}
		}
		
		// if affiliations are supplied by SOFD, validate that they all exist in FK Organisation
		if (affiliations != null) {
			for (Affiliation affiliation : affiliations) {
				boolean found = false;
				
				for (Position position : user.getPositions()) {
					if (Objects.equals(affiliation.getOrgUnitUuid(), position.getOrgUnitUuid())) {
						found = true;
						break;
					}
				}

				if (!found) {
					validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_WARNING, user.getUserId() + " mangler relation til enhed " + affiliation.getOrgUnitName() + " / " + affiliation.getOrgUnitUuid()));
				}
			}
		}

		// all is okay :)
		if (validations.size() == 0) {
			validations.add(new Validation(FK_ORG_VALIDATION_LABEL, Constants.STATUS_SUCCESS, user.getUserId() + " blev fundet"));
		}

		return validations;
	}
}
