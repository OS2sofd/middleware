package dk.digitalidentity.sofd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "settings")
public class Configuration {
	private String tabulexUrl = "https://api.ist.dk/medarbejder";
}
