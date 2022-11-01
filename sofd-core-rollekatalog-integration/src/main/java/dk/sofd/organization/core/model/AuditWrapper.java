package dk.sofd.organization.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditWrapper {
	private String uuid;
	private ChangeType changeType;
}