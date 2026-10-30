package dk.digitalidentity.sofd.logbuy.service.model;

import java.util.Set;
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
@EqualsAndHashCode
public class Person {
    private String uuid;
    private String cpr;
    private Set<User> users;
    private String firstname;
	private String surname;
    private Set<Affiliation> affiliations;
}
