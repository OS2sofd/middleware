package dk.digitalidentity.sofd.cics.service.kmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultWrapper {
	private String result;
	private String errorMessage;
}
