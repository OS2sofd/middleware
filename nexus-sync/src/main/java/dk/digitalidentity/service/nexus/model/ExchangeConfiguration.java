package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeConfiguration {
    private boolean sendToExchange;
    private boolean fetchFromExchange;
}
