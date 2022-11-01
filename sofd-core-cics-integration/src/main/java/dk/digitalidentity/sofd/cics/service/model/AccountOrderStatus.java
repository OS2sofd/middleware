package dk.digitalidentity.sofd.cics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountOrderStatus {
	private long id;
	private String affectedUserId;
	private String status;
	private String message;
}
