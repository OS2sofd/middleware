package dk.digitalidentity.brugertjek.integration.sofd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDTO {
	private boolean deleted;
	private String master;
	private String uuid;
	private String cpr;
	private String firstname;
	private String surname;
	private String chosenName;
	private List<Phone> phones;
	private List<Affiliation> affiliations;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime created;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastChanged;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate accountExpire;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate passwordExpire;
	
	private String kombitUuid;

	public PersonDTO(String userId, Person person) {
		this.deleted = person.isDeleted();
		this.master = person.getMaster();
		this.uuid = person.getUuid();
		this.cpr = person.getCpr();
		this.firstname = person.getFirstname();
		this.surname = person.getSurname();
		this.chosenName = person.getChosenName();
		this.phones = person.getPhones();
		this.created = person.getCreated();
		this.lastChanged = person.getLastChanged();
		
		if (person.getAffiliations() != null && !person.getAffiliations().isEmpty()) {
			this.affiliations = person.getAffiliations();
		}
		else {
			this.affiliations = new ArrayList<>();
		}
		
		if (person.getUsers() != null) {
			SofdUser sofdUser = person.getUsers().stream()
				.filter(u -> Objects.equals(u.getUserType(), "ACTIVE_DIRECTORY") && Objects.equals(u.getUserId().toLowerCase(), userId.toLowerCase()))
				.findFirst()
				.orElse(null);
			
			if (sofdUser != null) {
				if (StringUtils.hasText(sofdUser.getAccountExpireDate())) {
					try {
						this.accountExpire = LocalDate.parse(sofdUser.getAccountExpireDate());
					}
					catch (Exception ex) {
						; // ignore
					}
				}
				
				if (StringUtils.hasText(sofdUser.getPasswordExpireDate())) {
					try {
						this.passwordExpire = LocalDate.parse(sofdUser.getPasswordExpireDate());
					}
					catch (Exception ex) {
						; // ignore
					}
				}
				
				this.kombitUuid = sofdUser.getKombitUuid();
			}
		}
	}
}
