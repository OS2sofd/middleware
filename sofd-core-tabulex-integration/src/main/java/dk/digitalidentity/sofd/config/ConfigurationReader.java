package dk.digitalidentity.sofd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:default.properties" })
public class ConfigurationReader {

}