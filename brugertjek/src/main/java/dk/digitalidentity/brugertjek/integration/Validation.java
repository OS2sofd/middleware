package dk.digitalidentity.brugertjek.integration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Validation {
	private String name;
	private String status;
	private String message;
}
