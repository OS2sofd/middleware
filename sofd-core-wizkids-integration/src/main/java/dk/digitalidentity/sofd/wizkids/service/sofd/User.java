package dk.digitalidentity.sofd.wizkids.service.sofd;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.ALWAYS)
public class User {
    private String uuid;
    private String master;
    private String masterId;
    private String userId;
    private String userType;
	private boolean prime;
}
