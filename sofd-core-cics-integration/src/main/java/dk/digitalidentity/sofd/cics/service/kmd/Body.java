package dk.digitalidentity.sofd.cics.service.kmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {
	private SPMLSearchRequestResponse spmlSearchRequestResponse;
	private SPMLModifyRequestResponse spmlModifyRequestResponse;
	private SPMLAddRequestResponse spmlAddRequestResponse;
	private SPMLDeleteRequestResponse spmlDeleteRequestResponse;
}
