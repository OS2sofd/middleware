package dk.digitalidentity.sofd.cics.service.kmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SPMLDeleteRequestResponse {
	private String spmlDeleteRequestResult;
}
