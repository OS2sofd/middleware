package dk.digitalidentity.sofd.cics.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.model.KspUser;
import dk.digitalidentity.sofd.cics.service.model.Person;
import dk.digitalidentity.sofd.cics.service.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SyncService {
	private static final String kspCicsUserType = "KSPCICS";
	private static final String kspCicsMasterIdentifier = "KSPCICS";

	@Autowired
	private KspCicsService kspCicsService;
	
	@Autowired
	private SOFDService sofdService;

	public void execute(Municipality municipality) {
		try {
			List<KspUser> kspUsers = kspCicsService.loadAllCicsUsers(municipality);
			List<Person> persons = sofdService.getPersons(municipality);
			
			if (kspUsers == null || kspUsers.size() == 0) {
				log.error("Failed to fetch data from KSP/CICS");
				return;
			}
			
			if (persons == null || persons.size() == 0) {
				log.error("Failed to fetch data from SOFD");
				return;
			}
		
			update(municipality, kspUsers, persons);
		}
		catch (Exception ex) {
			log.error("Failed to synchronize " + municipality.getName(), ex);
		}
	}

	private void update(Municipality municipality, List<KspUser> kspUsers, List<Person> persons) {
		log.info("Processing " + kspUsers.size() + " KSP/CICS users for " + municipality.getName());

		Map<String, List<KspUser>> kspUsersMap = buildKspUserMap(kspUsers, municipality);
		
		long count = 0;
		for (Person person : persons) {
			// filter out those that are deleted
			if (person.isDeleted()) {
				continue;
			}
			
			List<KspUser> kspUsersForPerson = new ArrayList<>();
			if (kspUsersMap.containsKey(person.getCpr())) {
				kspUsersForPerson = kspUsersMap.get(person.getCpr());
			}

			// perform actual update, to ensure only those KSP/CICS users are present on this user
			if (handle(municipality, person, kspUsersForPerson)) {
				count++;
			}
		}
		
		log.info("Patched " + count + " persons");
	}

	private boolean handle(Municipality municipality, Person person, List<KspUser> kspUsersForPerson) {
		Person patchedPerson = new Person();

		handleUsers(kspUsersForPerson, person, patchedPerson);

		// an update is required if the patched person differs from a new empty
		if (!patchedPerson.equals(new Person())) {
			patchedPerson.setUuid(person.getUuid());
			try {
				sofdService.update(patchedPerson, municipality);
			}
			catch (Exception ex) {
				log.error("Failed to update " + person.getUuid(), ex);
			}
			
			return true;
		}
		
		return false;
	}

	private void handleUsers(List<KspUser> kspUsersForPerson, Person person, Person patchedPerson) {
		Set<User> sofdUsers = person.getUsers();
		if (sofdUsers == null) {
			sofdUsers = new HashSet<>();
		}

		boolean hasUsersChanged = false;

		// find entries to add
		for (KspUser kspUser : kspUsersForPerson) {
			boolean exists = sofdUsers.stream().anyMatch(u -> u.getMaster().equals(kspCicsMasterIdentifier) && u.getUserId().equalsIgnoreCase(kspUser.getUserId()));
			if (!exists) {
				hasUsersChanged = true;

				User mocesUser = new User();
				mocesUser.setMaster(kspCicsMasterIdentifier);
				mocesUser.setMasterId(kspUser.getUserId().toLowerCase());
				mocesUser.setUserId(kspUser.getUserId().toLowerCase());
				mocesUser.setUserType(kspCicsUserType);
				mocesUser.setUuid(UUID.randomUUID().toString());

				sofdUsers.add(mocesUser);
			}
		}
		
		// find entries to remove		
		for (Iterator<User> iterator = sofdUsers.iterator(); iterator.hasNext();) {
			User user = iterator.next();

			if (!user.getMaster().equals(kspCicsMasterIdentifier) || !user.getUserType().equals(kspCicsUserType)) {
				continue;
			}
			
			boolean shouldExist = kspUsersForPerson.stream().anyMatch(m -> user.getUserId().equalsIgnoreCase(m.getUserId()));			
			if (!shouldExist) {
				hasUsersChanged = true;

				iterator.remove();
			}
		}

		if (hasUsersChanged) {
			patchedPerson.setUsers(sofdUsers);
		}
	}

	private Map<String, List<KspUser>> buildKspUserMap(List<KspUser> kspUsers, Municipality municipality) {
		Map<String, List<KspUser>> result = new HashMap<>();
		
		for (KspUser user : kspUsers) {

			if (StringUtils.isEmpty(user.getCpr()) || StringUtils.isEmpty(user.getUserId())) {
				log.warn("Empty KSP/CICS entry in payload for municipality: " + municipality.getName());
				continue;
			}
			
			List<KspUser> users = result.get(user.getCpr());
			if (users == null) {
				users = new ArrayList<KspUser>();
				result.put(user.getCpr(), users);
			}
			
			users.add(user);
		}
		
		return result;
	}
}
