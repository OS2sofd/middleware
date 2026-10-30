package dk.digitalidentity.brugertjek.integration.sofd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliation {
	private String uuid;
	private String employeeId;
	private boolean prime;
	private String positionName;
	private String orgUnitUuid;
	private String orgUnitName; // requires additional lookup, starts NULL
	private String alternativeOrgUnitUuid;
	private String affiliationType;
	private String startDate;
	private String stopDate;
}
