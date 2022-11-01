package dk.digitalidentity.sofd.cics.service.model;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeltaSync {
    private int offset;
    private Collection<Change> uuids;
}
