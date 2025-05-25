package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferencesConfiguration {
    private long id;
    private String name;
    private boolean defaultPreference;
    private PreferenceCriteria criteria;
}
