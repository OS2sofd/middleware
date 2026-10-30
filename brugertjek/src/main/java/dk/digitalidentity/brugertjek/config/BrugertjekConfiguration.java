package dk.digitalidentity.brugertjek.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import dk.digitalidentity.brugertjek.config.modules.DevMode;
import dk.digitalidentity.brugertjek.config.modules.Nexus;
import dk.digitalidentity.brugertjek.config.modules.OS2sync;
import dk.digitalidentity.brugertjek.config.modules.OpsApi;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brugertjek")
public class BrugertjekConfiguration {
	private OpsApi opsapi = new OpsApi();
	private DevMode devMode = new DevMode();
	private OS2sync os2sync = new OS2sync();
	private Nexus nexus = new Nexus();
	private boolean scheduledEnabled;
}
