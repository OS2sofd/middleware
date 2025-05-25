package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultOrganizationSupplier {
    private boolean active;
    private long id;
    private long version;
    private String cvrNumber;
    private String name;
    private String type;
    private String organization;
    private long organizationId;
}
