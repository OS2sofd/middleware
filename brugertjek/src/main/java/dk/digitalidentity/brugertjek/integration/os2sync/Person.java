package dk.digitalidentity.brugertjek.integration.os2sync;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Person {

	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("Cpr")
	private String cpr;
}