package dk.digitalidentity.sofdcoreazureintegration.service.coredata;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CoreData {
	private String domain;
	private List<CoreDataEntry> entryList;
}
