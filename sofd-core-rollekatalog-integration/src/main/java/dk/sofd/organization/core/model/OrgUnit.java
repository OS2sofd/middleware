package dk.sofd.organization.core.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgUnit {
	private String uuid;
	private String name;
	private String parentUuid;
	private boolean inheritKle;
	private String managerUuid;
	private List<String> klePrimary;
	private List<String> kleSecondary;
	private List<String> titles;
}
