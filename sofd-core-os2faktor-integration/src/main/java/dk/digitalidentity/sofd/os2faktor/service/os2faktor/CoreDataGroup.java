package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CoreDataGroup {
	private String uuid;
	private String name;
	private String description;

	// samAccountNames of members (people with role)
	private List<String> members;
}
