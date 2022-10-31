package dk.sofd.opus.service.model;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Email {
	private String email;
	private String master;
	private String masterId;
}
