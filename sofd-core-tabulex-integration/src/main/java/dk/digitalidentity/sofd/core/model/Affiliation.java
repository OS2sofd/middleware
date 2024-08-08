package dk.digitalidentity.sofd.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliation {
	private String positionName;
	private String orgUnitUuid;
	private String startDate;
	private String stopDate;

	// Boolean to support null-check for sofd-versions that still do not send this attribute
	// change to boolean once all SOFD instances have version >= 2022-12-05
	private Boolean doNotInherit;
}
