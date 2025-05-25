package dk.digitalidentity.service.sofd.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
	private boolean deleted;
	private String uuid;
	private String cpr;
	private String firstname;
	private String surname;
	private String chosenName;
	private String authorizationCode;
	private List<Phone> phones;
	private List<SofdUser> users;
	private List<Affiliation> affiliations;
}
