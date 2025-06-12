package dk.digitalidentity.sofd.logbuy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "sofd")
public class SofdConfiguration {
	private String url;
	private String apiKey;
	private int customerId;
	private String logBuyUrl;
	private String logBuyApiKey;
}
