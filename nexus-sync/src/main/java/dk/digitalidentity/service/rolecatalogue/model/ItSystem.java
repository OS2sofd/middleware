package dk.digitalidentity.service.rolecatalogue.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItSystem {
    private long id;
    private String identifier;
    private String name;
    private boolean convertRolesEnabled;
    private List<ItSystemRole> systemRoles;
}
