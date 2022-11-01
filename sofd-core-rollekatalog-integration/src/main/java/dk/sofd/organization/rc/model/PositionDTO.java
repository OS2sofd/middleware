package dk.sofd.organization.rc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class PositionDTO {
	private String name;
	private String orgUnitUuid;
	private String titleUuid;
}
