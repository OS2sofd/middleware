package dk.sofd.organization.ad.service.model;

import java.util.Map;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class User {
    private String uuid;
    private String master;
    private String masterId;
    private String userId;
	private String employeeId;
    private String userType;
    private String passwordExpireDate; // actually a LocalDate on the other end, but we handle it as a String here for convenience
    private String accountExpireDate;  // actually a LocalDate on the other end, but we handle it as a String here for convenience
    private String whenCreated;        // actually a LocalDate on the other end, but we handle it as a String here for convenience
    private boolean prime;
    private Boolean disabled;
    private Boolean passwordLocked;
    private Map<String, String> localExtensions;
    private String upn;
    private String title;
}
