package dk.digitalidentity.brugertjek.integration.rc;

import java.util.Map;

import dk.digitalidentity.brugertjek.integration.Validation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCatalogValidation extends Validation {
	private Map<String, String> userRoles;

	public RoleCatalogValidation(String name, String status, String message, Map<String, String> userRoles) {
		super(name, status, message);
		this.userRoles = userRoles;
	}
}
