package dk.digitalidentity.sofd.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
	private String master;
	private String masterId;
	private String uuid;
	private String userId;
	private String userType;
	private String employeeId;
	private boolean disabled;
	private boolean substituteAccount;
	private Boolean passwordLocked;
	private String accountExpireDate;
	private String passwordExpireDate;
	private String upn;
	private String kombitUuid;
	private String whenCreated;
	private String title;
	private boolean prime;
}
