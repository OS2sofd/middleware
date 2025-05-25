package dk.digitalidentity.service.sofd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnit {
	private boolean deleted;
	private String uuid;
	private String id;
	private String parentUuid;
	private String name;
	private Set<Phone> phones;
	private Long ean;
	private Long pnr;
	private Set<OrgUnitPost> postAddresses;
}
