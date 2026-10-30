package dk.digitalidentity.sofd.logbuy.service.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonCreateResponse {
	private int totalCount;
	private int errors;
	private int success;
	private List<ErrorDetail> errorDetails;
}
