package dk.digitalidentity.service.sofd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnitPost {
	private String street;
	private String postalCode;
	private String city;
	private boolean prime;
}
