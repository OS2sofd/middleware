package dk.digitalidentity.sofd.cics.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KspUser {
	private String userId;
	private String cpr;
	private String department;
}
