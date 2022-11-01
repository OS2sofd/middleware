package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoreDataEntry {
	private String uuid;
	private String cpr;
	private String name;
	private String email;
	//separate Entry per ad account
	private String samAccountName;
	private boolean nsisAllowed;
	private boolean transferToNemlogin;
	private Map<String, String> attributes;

	public CoreDataEntry() {
		this.attributes = new HashMap<>();
	}
}