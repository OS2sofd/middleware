package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoreDataNemLoginStatus {
	private String domain;
	private Set<CoreDataNemLoginEntry> entries;
}
