package dk.sofd.organization.ad.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonsDto {
    @JsonProperty("_embedded")
    private PersonsEmbedded embedded;

    private Page page;
}
