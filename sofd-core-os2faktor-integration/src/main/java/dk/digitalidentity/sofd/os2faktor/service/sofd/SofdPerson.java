package dk.digitalidentity.sofd.os2faktor.service.sofd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofdPerson {
	private String uuid;
	private String name;
	private String userId;
	private String email;
	private String cpr;
	private String upn;
}
