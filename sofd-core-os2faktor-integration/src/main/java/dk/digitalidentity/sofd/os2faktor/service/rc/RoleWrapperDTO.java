package dk.digitalidentity.sofd.os2faktor.service.rc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleWrapperDTO {
	private List<RcUser> assignments;
}