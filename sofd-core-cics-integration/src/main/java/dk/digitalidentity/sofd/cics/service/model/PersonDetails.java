package dk.digitalidentity.sofd.cics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDetails {
	private String uuid;
	private String cpr;
	
	// internal use only
	private transient Person person;
}
