package dk.digitalidentity.sofd.logbuy.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
	private String access_token;
	private String token_type;
	private int expires_in;
}
