package dk.digitalidentity.service.sofd.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditWrapper {
	private String uuid;
	private ChangeType changeType;
}