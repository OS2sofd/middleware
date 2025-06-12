package dk.digitalidentity.sofd.logbuy.service.model;

import dk.digitalidentity.sofd.logbuy.dao.model.CreatedPerson;
import dk.digitalidentity.sofd.logbuy.dao.model.enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonCreateRequest {
	private String firstName;
	private String surName;
	private String email;
	private String salaryNumber;
	private Gender gender;
	
	public PersonCreateRequest(CreatedPerson person) {
		this.firstName = person.getFirstName();
		this.surName = person.getSurName();
		this.email = person.getEmail();
		this.salaryNumber = person.getSalaryNumber();
		this.gender = person.getGender();
	}
}
