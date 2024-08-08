package dk.digitalidentity.sofd.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
	private String uuid;
	private String cpr;
	private String master;
	private String firstname;
	private String surname;
	private boolean deleted;
	private String created;
	private List<User> users;
	private List<Affiliation> affiliations;
	private String chosenName;
	
	public String getMaskedCpr() {
		if (cpr == null) {
			return null;
		}
		
		if (cpr.length() != 10) {
			return cpr;
		}
		
		return cpr.substring(0, 6) + "-XXXX";
	}
}
