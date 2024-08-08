package dk.sofd.organization.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(exclude = { "cpr" })
@NoArgsConstructor
public class StudentApiRecord {

	// primary key
	private String username;

	// read/write fields
	private String cpr;
	private String name;
	private List<String> classes;
	private boolean disabled;
	private List<String> institutionNumbers;

	// readonly
	private String uuid;
}
