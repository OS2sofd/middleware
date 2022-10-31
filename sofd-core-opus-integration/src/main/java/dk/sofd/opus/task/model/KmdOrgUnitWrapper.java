package dk.sofd.opus.task.model;

import dk.kmd.opus.OrgUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class KmdOrgUnitWrapper {
	private dk.sofd.opus.service.model.OrgUnit sofdOrgUnit;
    private OrgUnit orgUnit;
    private String uuid;
    private boolean toBeUpdated;
    private boolean toBeCreated;

    private KmdOrgUnitWrapper parent;
    private List<KmdOrgUnitWrapper> children;
}
