package dk.sofd.organization.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SOFDSubstituteAssignment {
	private long id;
	private long substituteContextId;
	private String substituteContextName;
	private ManagerSubstitutePerson manager;
	private ManagerSubstitutePerson substitute;
	private List<OrgUnit> constraintOrgUnits;
}
