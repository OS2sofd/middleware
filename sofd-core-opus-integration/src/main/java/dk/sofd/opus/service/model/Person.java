package dk.sofd.opus.service.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString                     // TODO: we need to implement some better API to support null'ing these
@EqualsAndHashCode(exclude = { "firstEmploymentDate", "anniversaryDate" })
@NoArgsConstructor
@AllArgsConstructor
public class Person {
	private String uuid;
	private String master;
	private boolean deleted;
	private String cpr;
	private String firstname;
	private String surname;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate firstEmploymentDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate anniversaryDate;
	
	private Post registeredPostAddress;
	private Post residencePostAddress;
	private Set<User> users;
	private Set<Affiliation> affiliations;
	
	public Set<Affiliation> onlyActiveAffiliations() {
		if (affiliations == null || affiliations.size() == 0) {
			return new HashSet<Affiliation>();
		}
		
		return affiliations.stream().filter(a ->
			(a.getStartDate() == null || a.getStartDate().compareTo(LocalDate.now().toString()) <= 0) &&
			(a.getStopDate() == null || a.getStopDate().compareTo(LocalDate.now().toString()) >= 0) &&
			 a.isDeleted() == false)
		.collect(Collectors.toSet());
	}
}
