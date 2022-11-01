package dk.digitalidentity.sofd.cics.service.kmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEntry {
	private Identifier identifier;
	private Attributes attributes;
}
