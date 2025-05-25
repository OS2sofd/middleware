package dk.digitalidentity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "dk.digitalidentity" })
public class NexusSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusSyncApplication.class, args);
	}
}
