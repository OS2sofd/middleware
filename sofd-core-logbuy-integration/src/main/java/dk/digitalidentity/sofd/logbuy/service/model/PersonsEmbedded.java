package dk.digitalidentity.sofd.logbuy.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class PersonsEmbedded {
    private Collection<Person> persons;
}
