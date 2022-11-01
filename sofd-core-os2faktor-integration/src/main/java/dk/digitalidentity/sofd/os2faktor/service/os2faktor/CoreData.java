package dk.digitalidentity.sofd.os2faktor.service.os2faktor;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoreData {
	private String domain;
	private List<CoreDataEntry> entryList;
}