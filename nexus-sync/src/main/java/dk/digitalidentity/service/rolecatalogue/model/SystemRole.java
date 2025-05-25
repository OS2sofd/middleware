package dk.digitalidentity.service.rolecatalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemRole {
    private String roleName;
    private String roleIdentifier;
    private int weight;
}
