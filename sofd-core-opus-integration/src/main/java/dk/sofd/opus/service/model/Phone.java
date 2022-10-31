package dk.sofd.opus.service.model;

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
@EqualsAndHashCode(exclude = { "prime", "typePrime" })
public class Phone {
	private String master;
	private String masterId;
	private String phoneNumber;
	private String phoneType;
	private String visibility;
	private boolean prime;
	private boolean typePrime;
}
