package dk.digitalidentity.service.nexus.model;

public class NexusTimeoutException extends RuntimeException {
	private static final long serialVersionUID = -135498628338954984L;
	
	public NexusTimeoutException(String message, Throwable t) {
		super(message, t);
	}
}
