package dk.digitalidentity.brugertjek.integration.os2sync;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

	@JsonProperty("Uuid")
	private String uuid;

	@JsonProperty("ShortKey")
	private String shortKey;
	
	@JsonProperty("UserId")
	private String userId;
	
	@JsonProperty("PhoneNumber")
	private String phoneNumber;
	
	@JsonProperty("Landline")
	private String landline;
	
	@JsonProperty("Email")
	private String email;
	
	@JsonProperty("RacfID")
	private String racfID;
	
	@JsonProperty("Location")
	private String location;
	
	@JsonProperty("FMKID")
	private String fMKID;
	
	@JsonProperty("Status")
	private int status;
	
	@JsonProperty("Positions")
	private List<Position> positions = new ArrayList<>();
	
	@JsonProperty("Person")
	private Person person = new Person();

}