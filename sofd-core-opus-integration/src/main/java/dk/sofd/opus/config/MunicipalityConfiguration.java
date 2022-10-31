package dk.sofd.opus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "integration.settings")
public class MunicipalityConfiguration {
	private String s3FileshareUrl = "https://s3fileshare.digital-identity.dk";
}
