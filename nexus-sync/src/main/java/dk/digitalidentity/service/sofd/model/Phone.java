package dk.digitalidentity.service.sofd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Phone {
	private boolean prime;
	private boolean typePrime;
	private String phoneNumber;
	private String phoneType;
	private String visibility;
}
