package dk.digitalidentity.brugertjek.integration.entra;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntraUserLookup {
    private boolean active;
    private List<String> licenses;
}
