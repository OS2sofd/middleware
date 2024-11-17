package dk.digitalidentity.sofd.os2faktor.service.rc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRole {
    private long roleId;
    private String roleIdentifier;
    private String roleName;
    private String roleDescription;
    private List<SystemRole> systemRoles;
    private List<UserRoleAssignment> assignments;
}
