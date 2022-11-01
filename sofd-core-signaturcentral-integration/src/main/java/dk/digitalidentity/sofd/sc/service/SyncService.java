package dk.digitalidentity.sofd.sc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dk.digitalidentity.sofd.sc.api.model.MOCES;
import dk.digitalidentity.sofd.sc.dao.model.Municipality;
import dk.digitalidentity.sofd.sc.security.MunicipalityHolder;
import dk.digitalidentity.sofd.sc.service.model.Person;
import dk.digitalidentity.sofd.sc.service.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Component
public class SyncService {
	private static final String activeDirectoryUserType = "ACTIVE_DIRECTORY";
	private static final String mocesUserType = "MOCES";
	private static final String signaturcentralMasterIdentifier = "Signaturcentral";
	
	@Autowired
	private SOFDOrganizationService sofdOrganizationService;

	@Async
	public void sync(List<MOCES> moces, Municipality municipality) throws Exception {
		MunicipalityHolder.set(municipality);
		
		log.info("Starting full sync for: " + municipality.getName());

		List<Person> persons = sofdOrganizationService.getPersons();
		Map<String, List<MOCES>> mocesMap = buildMocesHashMap(moces, municipality);

		log.info("For '" + municipality.getName() + "' comparing " + moces.size() + " certificates against " + persons.size() + " persons");

		for (Person person : persons) {
			// filter out those that are deleted or has no users,
			// as we can neither ADD nor REMOVE MOCES users for those
			if (person.isDeleted() || person.getUsers() == null) {
				continue;
			}
			
			// identify all MOCES that matches AD accounts for this person
			List<MOCES> mocesForPerson = new ArrayList<>();
			List<User> adUsers = person.getUsers().stream()
					.filter(u -> u.getUserType().equals(activeDirectoryUserType))
					.collect(Collectors.toList());

			for (User adUser : adUsers) {
				List<MOCES> mocesEntries = mocesMap.get(adUser.getUserId().toLowerCase());
				if (mocesEntries != null) {
					mocesForPerson.addAll(mocesEntries);
				}
			}
			
			// perform actual update, to ensure only those MOCES are present on this user
			handle(person, mocesForPerson);
		}

		log.info("Completed full sync for: " + municipality.getName());
	}

	private void handle(Person person, List<MOCES> moces) throws Exception {
		Person patchedPerson = new Person();

		handleUsers(moces, person, patchedPerson);

		// an update is required if the patched person differs from a new empty
		if (!patchedPerson.equals(new Person())) {
			patchedPerson.setUuid(person.getUuid());
			sofdOrganizationService.update(patchedPerson);
		}
	}
	
	private void handleUsers(List<MOCES> moces, Person person, Person patchedPerson) {
		Set<User> sofdUsers = person.getUsers();
		if (sofdUsers == null) {
			sofdUsers = new HashSet<>();
		}

		boolean hasUsersChanged = false;

		// find MOCES entries to add
		for (MOCES mocesEntry : moces) {
			boolean exists = sofdUsers.stream().anyMatch(u -> u.getMaster().equals(signaturcentralMasterIdentifier) && u.getUserId().equalsIgnoreCase(mocesEntry.getRid()));
			if (!exists) {
				hasUsersChanged = true;

				User mocesUser = new User();
				mocesUser.setMaster(signaturcentralMasterIdentifier);
				mocesUser.setMasterId(mocesEntry.getUserId().toLowerCase());
				mocesUser.setUserId(mocesEntry.getRid());
				mocesUser.setUserType(mocesUserType);
				mocesUser.setUuid(UUID.randomUUID().toString());

				sofdUsers.add(mocesUser);
			}
		}
		
		// find MOCES entries to remove
		for (Iterator<User> iterator = sofdUsers.iterator(); iterator.hasNext();) {
			User user = iterator.next();

			if (!user.getMaster().equals(signaturcentralMasterIdentifier) || !user.getUserType().equals(mocesUserType)) {
				continue;
			}
			
			boolean shouldExist = moces.stream().anyMatch(m -> user.getUserId().equalsIgnoreCase(m.getRid()));			
			if (!shouldExist) {
				hasUsersChanged = true;

				iterator.remove();
			}
		}

		if (hasUsersChanged) {
			patchedPerson.setUsers(sofdUsers);
		}
	}

	private Map<String, List<MOCES>> buildMocesHashMap(List<MOCES> moces, Municipality municipality) {
		Map<String, List<MOCES>> result = new HashMap<>();
		
		for (MOCES mocesEntry : moces) {
			if (StringUtils.isEmpty(mocesEntry.getUserId()) || StringUtils.isEmpty(mocesEntry.getSubject())) {
				log.warn("Empty MOCES entry in payload for municipality: " + municipality.getName());
				continue;
			}
			
			String userId = mocesEntry.getUserId().toLowerCase();
			
			List<MOCES> userMoces = result.get(userId);
			if (userMoces == null) {
				userMoces = new ArrayList<MOCES>();
				result.put(userId, userMoces);
			}
			
			userMoces.add(mocesEntry);
		}
		
		return result;
	}
}
