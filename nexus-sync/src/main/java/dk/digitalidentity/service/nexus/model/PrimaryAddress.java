package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrimaryAddress {
    private String addressLine1;
    private String postalDistrict;
    private String postalCode;
    private String countryCode;
}
