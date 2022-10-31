package dk.sofd.opus.service.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ADAccountOrderApiOrderDTO {
	private String personUuid;
	private String employeeId;
	private Date activationTimestamp;
}
