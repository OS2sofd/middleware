package dk.digitalidentity.service.rolecatalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RolesAsListDTO {
	private List<String> systemRoles;
}
