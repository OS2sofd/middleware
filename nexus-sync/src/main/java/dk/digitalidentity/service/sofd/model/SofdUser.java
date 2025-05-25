package dk.digitalidentity.service.sofd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofdUser {
	private String uuid;
	private String userType;
	private String userId;
	private String employeeId;
	private Boolean disabled;
	private boolean prime;
	private String upn;
	private String kombitUuid;
	private String title;
}
