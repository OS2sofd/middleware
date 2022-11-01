package dk.sofd.organization.rc.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgUnitDTO {
	private String uuid;
	private String name;
	private String parentOrgUnitUuid;
	private boolean inheritKle;
	private List<String> klePerforming;
	private List<String> kleInterest;
	private List<Long> itSystemIdentifiers;
	private ManagerDTO manager;
	private List<String> titleIdentifiers;
}
