package dk.digitalidentity.brugertjek.integration.rc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCatalogValidationService {
	private final String RC_VALIDATION_LABEL = "OS2rollekatalog";
	private final RoleCatalogService roleCatalogService;

	public RoleCatalogueRolesResponse getRoles(Municipality municipality, String userId) {
		try {
			return roleCatalogService.lookupRoles(municipality, userId);
		}
		catch (Exception ex) {
			log.warn("Could not connect to RoleCatalog", ex);

			return null;
		}
	}
	
	public List<Validation> validate(Municipality municipality, String userId, Map<String, String> userRoles, boolean disabled) {
		if (userRoles == null) {
			return Collections.singletonList(new Validation(RC_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev ikke fundet"));
		}

		if (disabled) {
			return Collections.singletonList(new Validation(RC_VALIDATION_LABEL, Constants.STATUS_ERROR, userId + " blev fundet - men er spærret"));			
		}

		if (userRoles.size() == 0) {
			return Collections.singletonList(new Validation(RC_VALIDATION_LABEL, Constants.STATUS_WARNING, userId + " blev fundet - men er ikke tildelt nogen roller"));			
		}

		return Collections.singletonList(new Validation(RC_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
	}
}
