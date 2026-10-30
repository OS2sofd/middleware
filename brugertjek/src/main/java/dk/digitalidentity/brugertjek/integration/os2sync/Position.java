package dk.digitalidentity.brugertjek.integration.os2sync;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {
	
	@JsonProperty("Name")
	private String name;

	@JsonProperty("OrgUnitUuid")
	private String orgUnitUuid;

	@JsonProperty("StartDate")
	private LocalDate startDate;
	
	@JsonProperty("StopDate")
	private LocalDate stopDate;
}