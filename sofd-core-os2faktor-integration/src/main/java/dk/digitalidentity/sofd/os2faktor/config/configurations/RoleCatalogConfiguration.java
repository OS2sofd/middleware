package dk.digitalidentity.sofd.os2faktor.config.configurations;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCatalogConfiguration {
	private boolean enabled = true;
	private String url;
	private String apiKey;
	private String roleId;
}
