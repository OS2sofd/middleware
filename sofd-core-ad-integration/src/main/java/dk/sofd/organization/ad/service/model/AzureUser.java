package dk.sofd.organization.ad.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureUser {
    private String userPrincipalName;
    private String id;
    private String onPremisesSamAccountName;
}
