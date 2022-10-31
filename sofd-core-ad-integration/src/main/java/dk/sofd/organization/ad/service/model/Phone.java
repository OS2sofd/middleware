package dk.sofd.organization.ad.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import dk.sofd.organization.ad.service.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class Phone {
	private String master;
	private String masterId;
	private String phoneNumber;
	private String phoneType;
	private Visibility visibility;
}
