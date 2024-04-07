package dk.digitalidentity.sofdcoreazureintegration.service.sofd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
	private String uuid;
	private String master;
	private String masterId;
	private String userId;
	private String userType;
	private String passwordExpireDate; // actually a LocalDate on the other end, but we handle it as a String here for convenience
	private String accountExpireDate;  // actually a LocalDate on the other end, but we handle it as a String here for convenience
}