package dk.sofd.organization.core.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncResult {
	private Long offset;
	private List<AuditWrapper> uuids;
}