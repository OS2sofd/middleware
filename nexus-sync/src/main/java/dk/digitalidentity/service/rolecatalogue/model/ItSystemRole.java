package dk.digitalidentity.service.rolecatalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItSystemRole {
    private String name;
    private String identifier;
    private String description;
}
