package dk.digitalidentity.brugertjek.integration.nexus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeDTO {
	private long nexusId;
	private boolean active;
	private String nexusSyncStatus;
	private String identityId;
}
