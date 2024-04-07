package dk.digitalidentity.sofdcoreazureintegration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:default.properties" }, ignoreResourceNotFound = true)
public class ConfigurationReader {

}