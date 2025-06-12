package dk.digitalidentity.sofd.logbuy.service.model;

import dk.digitalidentity.sofd.logbuy.dao.model.CreatedPerson;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDeleteRequest {
	private boolean disableWithoutDelete;
	private String salaryNumber;
	
	public PersonDeleteRequest(CreatedPerson person) {
		this.disableWithoutDelete = false;
		this.salaryNumber = person.getUuid();
	}
}
