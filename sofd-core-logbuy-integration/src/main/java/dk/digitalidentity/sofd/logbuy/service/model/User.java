package dk.digitalidentity.sofd.logbuy.service.model;

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
