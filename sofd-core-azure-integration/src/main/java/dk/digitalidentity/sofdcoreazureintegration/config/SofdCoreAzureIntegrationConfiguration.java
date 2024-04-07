package dk.digitalidentity.sofdcoreazureintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofdcoreazureintegration.config.modules.AzureAd;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "sofd.azure")
public class SofdCoreAzureIntegrationConfiguration {
	private AzureAd azureAd = new AzureAd();
	private String masterIdentifier;
}
