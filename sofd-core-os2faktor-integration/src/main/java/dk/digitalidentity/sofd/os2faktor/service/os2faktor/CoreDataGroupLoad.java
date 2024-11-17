package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CoreDataGroupLoad {
	private String domain;
	private List<CoreDataGroup> groups;
}
