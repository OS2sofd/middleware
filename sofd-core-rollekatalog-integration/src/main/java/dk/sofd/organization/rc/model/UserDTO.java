package dk.sofd.organization.rc.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
	private String extUuid;
	private String userId;
	private String name;
	private String email;
	private String phone;
	private String cpr;
	private boolean doNotInherit;
	private boolean disabled;
	private List<PositionDTO> positions;
	private List<String> klePerforming;
	private List<String> kleInterest;
}
