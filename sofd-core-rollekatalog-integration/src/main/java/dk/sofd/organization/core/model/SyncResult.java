package dk.sofd.organization.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncResult {
	private Long offset;
	private List<AuditWrapper> uuids;
}