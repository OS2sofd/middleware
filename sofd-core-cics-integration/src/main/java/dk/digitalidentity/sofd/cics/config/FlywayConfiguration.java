package dk.digitalidentity.sofd.cics.config;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class FlywayConfiguration {
	
	@Value("${flyway.clean:false}")
	private boolean wipeDatabase;

	@Bean(initMethod = "migrate")
	public Flyway flyway(DataSource dataSource) {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);

		if (wipeDatabase) {
			flyway.clean();
		}
		
		return flyway;
	}
}
