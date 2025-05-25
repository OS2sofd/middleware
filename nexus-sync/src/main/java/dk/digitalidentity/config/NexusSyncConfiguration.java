package dk.digitalidentity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus")
public class NexusSyncConfiguration {
	private boolean emailEnabled = false;
	private String emailHost;
	private String emailUsername;
	private String emailPassword;
	private String syncApiKey;
}
