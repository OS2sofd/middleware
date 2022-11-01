package dk.digitalidentity.sofd.cics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountOrder {
	private long id;
	private PersonDetails person;
	private String orderType;
	private String userId;
	
	// internal use only
	private transient String status;
	private transient String message;
}
