package dk.sofd.organization.core.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Person {
	private String uuid;
	private String name;
	private String userId;
	private String email;
	private String phone;
	private String cpr;
	private boolean prime;
	private boolean doNotInherit;
	private boolean disabled;
	private List<Affiliation> affiliations;
	private List<String> klePrimary;
	private List<String> kleSecondary;
}
