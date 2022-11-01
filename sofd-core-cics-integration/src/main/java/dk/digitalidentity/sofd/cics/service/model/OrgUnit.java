package dk.digitalidentity.sofd.cics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnit {
	private String uuid;
	private String shortname;
}
