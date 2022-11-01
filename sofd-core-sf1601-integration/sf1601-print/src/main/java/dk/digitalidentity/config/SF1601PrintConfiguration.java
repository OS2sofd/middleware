package dk.digitalidentity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "print")
public class SF1601PrintConfiguration {
	private String getSamlTokenUrl = "https://adgangsstyring.stoettesystemerne.dk/runtime/api/rest/wstrust/v1/issue";
	private String servicePlatformenBaseUrl = "https://prod.serviceplatformen.dk";
	private KeystoreConfig keystore = new KeystoreConfig();
}