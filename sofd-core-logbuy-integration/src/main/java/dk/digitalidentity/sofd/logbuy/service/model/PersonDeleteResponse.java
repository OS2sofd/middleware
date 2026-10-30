package dk.digitalidentity.sofd.logbuy.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDeleteResponse {
	private int processedSuccessfully;
	private int errors;
	private int messages;
}
