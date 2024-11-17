package dk.digitalidentity.sofd.os2faktor.service.sofd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofdPerson {
	private String uuid;
	private String cpr;
	private String name;
	private String userId;
	private boolean disabled;
	private boolean expired;
	private String email;
	private String upn;
	private String primaryOrgunitName;
	private String passwordExpireDate;
	private String localExtensions;
}
