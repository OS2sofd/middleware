package dk.sofd.organization.rc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ResponseDTO {
	private long usersCreated;
	private long usersUpdated;
	private long usersDeleted;
	private long ousCreated;
	private long ousUpdated;
	private long ousDeleted;
	
	public boolean containsChanges() {
		if (usersCreated > 0 || usersUpdated > 0 || usersDeleted > 0 ||
			ousCreated > 0 || ousUpdated > 0 || ousDeleted > 0) {
			return true;
		}
		
		return false;
	}
}
