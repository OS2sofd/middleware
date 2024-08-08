package dk.sofd.organization.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InstitutionApiRecord {
	private String name;
	private String institutionNumber;

	// read-only
	private String uuid;
}
