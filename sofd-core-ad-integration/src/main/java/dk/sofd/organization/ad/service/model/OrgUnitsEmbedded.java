package dk.sofd.organization.ad.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrgUnitsEmbedded {
    private List<OrgUnit> orgUnits;
}
