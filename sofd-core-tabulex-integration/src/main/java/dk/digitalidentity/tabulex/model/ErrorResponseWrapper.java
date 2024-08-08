package dk.digitalidentity.tabulex.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseWrapper {
	private String error;
	
	@Override
	public String toString() {
		return error;
	}
}
