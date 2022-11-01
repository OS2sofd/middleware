package dk.digitalidentity.sofd.sc.config;

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
	private String password;
	
	private String sofdUrl;
	private String sofdApiKey;
	
	private transient String clientVersion;
}
