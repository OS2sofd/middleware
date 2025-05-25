package dk.digitalidentity.service.rolecatalogue.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRole {
    private long roleId;
    private String roleIdentifier;
    private String roleName;
    private List<SystemRole> systemRoles;
    private List<UserRoleAssignment> assignments;
}
