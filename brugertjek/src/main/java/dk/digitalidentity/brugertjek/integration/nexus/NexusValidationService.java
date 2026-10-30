package dk.digitalidentity.brugertjek.integration.nexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NexusValidationService {
	private final String NEXUS_VALIDATION_LABEL = "KMD Nexus";
	private final NexusService nexusService;

	public EmployeeDTO getStatus(Municipality municipality, String userId) {
		try {
			return nexusService.getStatusByUserId(municipality, userId);
		}
		catch (Exception ex) {
			log.warn("Could not connect to KMD Nexus", ex);

			return null;
		}
	}
	
	public List<Validation> validate(Municipality municipality, String userId, EmployeeDTO employee) {
		if (employee == null) {
			return Collections.singletonList(new Validation(NEXUS_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev ikke fundet"));
		}

		List<Validation> validations = new ArrayList<>();
		if (!employee.isActive()) {
			return Collections.singletonList(new Validation(NEXUS_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " er ikke aktiv"));			
		}

		if (!Objects.equals("Synkroniseret", employee.getNexusSyncStatus())) {
			return Collections.singletonList(new Validation(NEXUS_VALIDATION_LABEL, Constants.STATUS_WARNING, userId + " er ikke synkroniseret af NexusSync"));			
		}

		// all is okay :)
		if (validations.size() == 0) {
			validations.add(new Validation(NEXUS_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
		}

		return validations;
	}
}
