package dk.sofd.core.stil.service.model;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonsEmbedded {
	private Collection<Person> persons;
}
