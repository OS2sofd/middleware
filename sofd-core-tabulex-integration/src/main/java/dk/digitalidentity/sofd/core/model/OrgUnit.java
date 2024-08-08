package dk.digitalidentity.sofd.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnit {
	private String uuid;
	private String name;
	private String masterId;
	private String parentUuid;
	private List<Tag> tags;
}
