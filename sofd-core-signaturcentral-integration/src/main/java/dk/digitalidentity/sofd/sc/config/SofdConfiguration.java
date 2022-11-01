package dk.digitalidentity.sofd.sc.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "sofd")
public class SofdConfiguration {
	@Deprecated
	private List<Municipality> municipalities;
}
