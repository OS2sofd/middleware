package dk.digitalidentity.brugertjek.config.modules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Nexus {
	private String url = "https://nexussync.digital-identity.dk";
	private String apiKey;
}
