package dk.sofd.opus.service.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpusFilterRulesDTO {
	private boolean enabled;
	private List<String> positionIds;
	private List<String> losIds;
	private String orgUnitInfix;
}
