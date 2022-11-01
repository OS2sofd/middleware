package dk.digitalidentity.sofd.os2faktor.service.rc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RcUser {
	private String userId;
	private String extUuid;
}