package dk.sofd.organization.rc.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class OrganisationDTO {
	private List<UserDTO> users;
	private List<OrgUnitDTO> orgUnits;
}