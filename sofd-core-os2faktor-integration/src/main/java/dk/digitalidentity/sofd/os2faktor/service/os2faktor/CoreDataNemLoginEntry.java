package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
public class CoreDataNemLoginEntry {
	private String cpr;
	private String samAccountName;
	private String nemloginUserUuid;	
	private boolean active;
}
