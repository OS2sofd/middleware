package dk.sofd.organization.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class StudentResult {
	private Set<StudentApiRecord> students;
	private long page;
}
