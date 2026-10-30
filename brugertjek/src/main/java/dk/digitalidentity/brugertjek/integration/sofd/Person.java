package dk.digitalidentity.brugertjek.integration.sofd;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	private String master;
	private String uuid;
	private String cpr;
	private String firstname;
	private String surname;
	private String chosenName;
	private String authorizationCode;
	private List<Phone> phones = new ArrayList<Phone>();
	private List<SofdUser> users = new ArrayList<SofdUser>();
	private List<Affiliation> affiliations = new ArrayList<Affiliation>();
	private Post registeredPostAddress;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime created;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastChanged;
}
