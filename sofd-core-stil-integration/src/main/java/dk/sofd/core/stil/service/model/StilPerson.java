package dk.sofd.core.stil.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StilPerson {
	private String cpr;
	private String uniLogin;
	private String firstname;
	private String surname;
	private String email;
	private String streetAddress;
	private String city;
	private long postalCode;
	private String institutionName;
	private String institutionNumber;
	private String occupation;
	private String roles;
	private String groups;
}
