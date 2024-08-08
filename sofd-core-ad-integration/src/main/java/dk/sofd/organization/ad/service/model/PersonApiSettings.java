package dk.sofd.organization.ad.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonApiSettings {
	private boolean activeDirectoryEmployeeIdAssociationEnabled;

}
