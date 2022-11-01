package dk.digitalidentity.sofd.cics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.model.Affiliation;
import dk.digitalidentity.sofd.cics.service.model.KspUser;
import dk.digitalidentity.sofd.cics.service.model.Person;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MoveService {

	@Autowired
	private KspCicsService kspCicsService;
	
	@Autowired
	private SOFDService sofdService;

	public void execute(Municipality municipality) {
		try {
			if (municipality.getHead() == 0L) {
				fullSync(municipality);
			}
			else {
				deltaSync(municipality);
			}
		}
		catch (Exception ex) {
			log.error("Failed to move users for " + municipality.getName(), ex);
		}
	}

	private void fullSync(Municipality municipality) throws Exception {
		log.info("Performing fullSync move of department on CICS accounts for " + municipality.getName());

		// keep track of head for next update
		municipality.setHead(sofdService.getHead(municipality));

		List<Person> persons = sofdService.getPersons(municipality);

		move(municipality, persons);
	}
	
	private void deltaSync(Municipality municipality) {
		log.info("Performing deltaSync move of department on CICS accounts for " + municipality.getName());
		
		Set<String> uuids = sofdService.getDeltaPersons(municipality, municipality.getHead());
		if (uuids.size() == 0) {
			return;
		}
		
		List<Person> persons = new ArrayList<>();
		for (String uuid : uuids) {
			Person person = sofdService.getPerson(uuid, municipality);
			if (person != null) {
				persons.add(person);
			}
		}
		
		move(municipality, persons);
	}
	
	private void move(Municipality municipality, List<Person> persons) {
		List<KspUser> kspUsers = kspCicsService.loadAllCicsUsers(municipality);

		int count = 0;
		for (KspUser kspUser : kspUsers) {
			for (Person person : persons) {
				if (person.getCpr().equals(kspUser.getCpr())) {
					
					// need a prime affiliation, otherwise we cannot do anything
					Optional<Affiliation> affilationResult = person.getAffiliations().stream().filter(a -> a.isPrime()).findFirst();
					if (!affilationResult.isPresent()) {
						break;
					}
					
					// and we need an actual department
					String uuid = affilationResult.get().getOrgUnitUuid();
					String department = sofdService.getOrgUnitLOSId(municipality, uuid);
					if (department == null) {
						break;
					}

					// do we need to move the user?
					if (!kspUser.getDepartment().equals(department)) {
						kspCicsService.moveUser(municipality, person, kspUser.getUserId());
						count++;
					}
				}
			}

		}
		
		log.info("Moved department for " + count + " user(s)");
	}
}
