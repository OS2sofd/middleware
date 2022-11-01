package dk.digitalidentity.sofd.cics.service.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KspUserProfilesResponse {
	List<KspUserProfile> userProfiles;
}
