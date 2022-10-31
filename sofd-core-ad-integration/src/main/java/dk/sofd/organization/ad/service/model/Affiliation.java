package dk.sofd.organization.ad.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(exclude = { "uuid" })
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Affiliation {
	private String uuid;
	private String master;
	private String masterId;
	private String startDate;
	private String stopDate;
	private boolean deleted;
	private String orgUnitUuid;
	private String affiliationType;
	private String positionName;
	private String localExtensions;
}
