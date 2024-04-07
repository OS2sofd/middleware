package dk.digitalidentity.sofdcoreazureintegration.service.coredata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoreDataEntry {
	private String uuid;
	private String cpr;
	private String firstName;
	private String lastName;
	private String email;
	private String userId;

	@JsonIgnore
	private transient String azureInternalId;

}