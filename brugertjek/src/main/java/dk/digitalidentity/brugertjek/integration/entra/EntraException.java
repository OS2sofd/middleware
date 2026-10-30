package dk.digitalidentity.brugertjek.integration.entra;

public class EntraException extends Exception {
	private static final long serialVersionUID = 1L;

	public EntraException(String errorMessage) {
		super(errorMessage);
	}
}
