package dk.digitalidentity.sofd.cics.service.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class Person {
    private String uuid;
    private boolean deleted;
    private String cpr;
    private Set<User> users;
    private String firstname;
	private String surname;

	// will be set to NULL in SOFD Service when PATCH'ing to avoid overwriting data
    private Set<Affiliation> affiliations;
}
