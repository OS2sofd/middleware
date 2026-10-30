package dk.digitalidentity.sofd.wizkids.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @deprecated
 */
@Deprecated
@Getter
@Setter
public class Municipality {
	private String name;

	private String cicsKeystore;
	private String cicsPassword;
	private String cicsLosId;
	
	private String sofdUrl;
	private String sofdApiKey;
	
	private boolean accountOrdersEnabled;
	
	// transient value, stored when sync'ing
	private long head;
}
