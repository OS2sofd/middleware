package dk.sofd.organization.ad.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnitsDto {

    @JsonProperty("_embedded")
    private OrgUnitsEmbedded embedded;

    private Page page;
}
