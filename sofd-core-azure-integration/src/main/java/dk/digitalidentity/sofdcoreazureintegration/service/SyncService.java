package dk.digitalidentity.sofdcoreazureintegration.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofdcoreazureintegration.config.SofdCoreAzureIntegrationConfiguration;
import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;
import dk.digitalidentity.sofdcoreazureintegration.service.coredata.CoreDataEntry;
import dk.digitalidentity.sofdcoreazureintegration.service.sofd.SofdService;
import dk.digitalidentity.sofdcoreazureintegration.service.sofd.model.Person;
import dk.digitalidentity.sofdcoreazureintegration.service.sofd.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncService {

	private static final String USERTYPE_AZUREEMAIL = "AZUREEMAIL";
	private static final String USERTYPE_AZUREAD = "AZUREAD";

	@Autowired
	private SofdCoreAzureIntegrationConfiguration configuration;
	
	@Autowired
	private SofdService sofdOrganizationService;
	
	@Transactional
	public void fullSyncMunicipality(Municipality municipality, List<CoreDataEntry> coreDataUsers) {
		try {
			Collection<Person> sofdPersons = sofdOrganizationService.getPersons(municipality);

			log.info("Full Synchronizing " + coreDataUsers.size() + " users from AzureAD for " + municipality.getName());
			fullSync(coreDataUsers, sofdPersons, municipality);
		}
		catch (Exception ex) {
			log.error("Full Synchronization failed for " + municipality.getName(), ex);
		}
	}

	@Transactional
	public void deltaSyncMunicipality(Municipality municipality, List<CoreDataEntry> coreDataUsers) {
		try {
			Collection<Person> sofdPersons = sofdOrganizationService.getPersons(municipality);

			log.info("Delta Synchronizing " + coreDataUsers.size() + " users from AzureAD for " + municipality.getName());
			deltaSync(coreDataUsers, sofdPersons, municipality);
		}
		catch (Exception ex) {
			log.error("Delta Synchronization failed for " + municipality.getName(), ex);
		}
	}


	private void deltaSync(Collection<CoreDataEntry> coreDataUsers, Collection<Person> sofdPersons, Municipality municipality) throws Exception {
		merge(buildCoreDataUserHashMap(coreDataUsers), buildPersonHashMap(sofdPersons), municipality, false);
	}

	private void fullSync(Collection<CoreDataEntry> coreDataUsers, Collection<Person> sofdPersons, Municipality municipality) throws Exception {
		merge(buildCoreDataUserHashMap(coreDataUsers), buildPersonHashMap(sofdPersons), municipality, true);
	}

	private void merge(Map<String, List<CoreDataEntry>> coreDataUsers, HashMap<String, Person> persons, Municipality municipality, boolean fullSync) throws Exception {
		long createCount = 0;
		long updateCount = 0;
		long deleteCount = 0;

		// find person elements that need to be created. ie. elements that are not in sofd organization
		HashSet<String> toBeCreated = new HashSet<>(coreDataUsers.keySet());
		toBeCreated.removeAll(persons.keySet());
		for (String cpr : toBeCreated) {
			handleCreate(coreDataUsers.get(cpr), municipality);
			createCount++;
		}

		// find person elements that need to be updated. ie. the intersection of both sets
		HashSet<String> toBeUpdated = new HashSet<>(coreDataUsers.keySet());
		toBeUpdated.retainAll(persons.keySet());
		for (String cpr : toBeUpdated) {
			if (handleUpdate(coreDataUsers.get(cpr), persons.get(cpr), municipality, fullSync)) {
				updateCount++;
			}
		}

		if (fullSync) {
			// find person elements that need to be deleted. ie. elements that are not in AzureAD
			HashSet<String> toBeDeleted = new HashSet<>(persons.keySet());
			toBeDeleted.removeAll(coreDataUsers.keySet());
			for (String cpr : toBeDeleted) {
				if (handleDelete(persons.get(cpr), municipality)) {
					deleteCount++;
				}
			}
		}

		log.info("Created " + createCount + ", Updated " + updateCount + ", Deleted " + deleteCount + " for " + municipality.getName());
	}

	private void handleCreate(List<CoreDataEntry> coreDataUsers, Municipality municipality) throws Exception {
		CoreDataEntry anyUser = coreDataUsers.stream().findAny().orElse(null);
		if (anyUser != null) {
			Person person = new Person();
			person.setMaster(configuration.getMasterIdentifier());
			person.setCpr(anyUser.getCpr());
			person.setFirstname(anyUser.getFirstName());
			person.setSurname(anyUser.getLastName());
			person.setUsers(new HashSet<>());
	
			for (CoreDataEntry coreDataUser : coreDataUsers) {
				User user = new User();
				user.setUuid(UUID.randomUUID().toString());
				inflateUserFromCoreDataUser(user, coreDataUser);
				person.getUsers().add(user);
		
				addEmailToPerson(person.getUsers(), coreDataUser, user.getUuid());
			}

			sofdOrganizationService.create(person, municipality);
		}
	}

	private boolean handleUpdate(List<CoreDataEntry> coreDataUsers, Person person, Municipality municipality, boolean fullSync) throws Exception {
		Person patchedPerson = fullSync ? person : new Person();
		boolean shouldUpdate = false;

		// if the person is deleted, revive it and take control of it
		if (person.isDeleted()) {
			shouldUpdate = true;
			patchedPerson.setMaster(configuration.getMasterIdentifier());
			patchedPerson.setDeleted(false); // not a big fan of middleware deciding to undelete persons, but it saves waiting for the next nightly run
		}

		Set<User> users = person.getUsers();

		List<User> sofdUsers = users.stream()
				.filter(u -> u.getMaster().equalsIgnoreCase(configuration.getMasterIdentifier()) && u.getUserType().equals(USERTYPE_AZUREAD))
				.collect(Collectors.toList());

		List<User> mailUsers = users.stream()
				.filter(u -> u.getMaster().equalsIgnoreCase(configuration.getMasterIdentifier()) && u.getUserType().equals(USERTYPE_AZUREEMAIL))
				.collect(Collectors.toList());

		for (User sofdUser : sofdUsers) {
			CoreDataEntry coreDataUser = coreDataUsers.stream().filter(coreUser -> sofdUser.getMasterId().equalsIgnoreCase(coreUser.getUuid())).findAny().orElse(null);
			if (coreDataUser != null) {
				// found a matching user. We should update it

				// create a copy of the user object
				User originalUser = sofdUser.toBuilder().build();
				inflateUserFromCoreDataUser(sofdUser, coreDataUser);

				// patch if user differs from original
				if (!sofdUser.equals(originalUser)) {
					shouldUpdate = true;

					patchedPerson.setUsers(users);
				}
				
				// Find mail user
				User existingMailUser = mailUsers.stream().filter(user -> user.getMasterId().equalsIgnoreCase(sofdUser.getUuid())).findAny().orElse(null);
				if (existingMailUser != null) {
					if (!Objects.equals(existingMailUser.getUserId(), coreDataUser.getEmail())) {
						existingMailUser.setUserId(coreDataUser.getEmail());

						patchedPerson.setUsers(users);
						shouldUpdate = true;
					}
				} else {
					addEmailToPerson(patchedPerson.getUsers(), coreDataUser, sofdUser.getUuid());
					patchedPerson.setUsers(users);
					shouldUpdate = true;
				}
				
			} else if (coreDataUser == null && fullSync) {
				// User found in SOFD but not in AzureAD so Delete
				users.remove(sofdUser);
				
				//Also delete mail user
				User existingMailUser = mailUsers.stream().filter(user -> user.getMasterId().equalsIgnoreCase(sofdUser.getUuid())).findAny().orElse(null);
				if (existingMailUser !=null) {
					users.remove(existingMailUser);
				}
				
				patchedPerson.setUsers(users);

				shouldUpdate = true;
			}
		}
		
		for (CoreDataEntry azureAdUser : coreDataUsers) {
			User sofdUser = users.stream().filter(u -> u.getMasterId().equalsIgnoreCase(azureAdUser.getUuid())).findAny().orElse(null);
			if (sofdUser == null) {
				// no corresponding user was found. We should add one
				User user = new User();
				user.setUuid(UUID.randomUUID().toString());
				inflateUserFromCoreDataUser(user, azureAdUser);

				users.add(user);
				patchedPerson.setUsers(users);

				addEmailToPerson(patchedPerson.getUsers(), azureAdUser, user.getUuid());

				shouldUpdate = true;
			}
		}

		if (shouldUpdate) {
			patchedPerson.setUuid(person.getUuid());
			sofdOrganizationService.update(patchedPerson, municipality);
		}

		return shouldUpdate;
	}

	private boolean handleDelete(Person person, Municipality municipality) throws Exception {
		Person patchedPerson = new Person();
		patchedPerson.setUuid(person.getUuid());
		boolean shouldUpdate = false;

		// remove all AzureAD users from this person
		HashSet<User> nonCoreDataUsers = new HashSet<>();
		for (User user : person.getUsers()) {
			if (user.getMaster().equalsIgnoreCase(configuration.getMasterIdentifier())) {
				shouldUpdate = true;
			}
			else {
				nonCoreDataUsers.add(user);
			}
		}

		patchedPerson.setUsers(nonCoreDataUsers);

		if (shouldUpdate) {
			sofdOrganizationService.update(patchedPerson, municipality);
		}

		return shouldUpdate;
	}

	private void addEmailToPerson(Set<User> personUsers, CoreDataEntry coreDataUser, String userUuid) {
		User mailUser = new User();

		mailUser.setUserType(USERTYPE_AZUREEMAIL);
		mailUser.setUuid(UUID.randomUUID().toString());
		mailUser.setMaster(configuration.getMasterIdentifier());
		mailUser.setMasterId(userUuid);
		mailUser.setUserId(coreDataUser.getEmail());
		mailUser.setAccountExpireDate("9999-12-31");
		mailUser.setPasswordExpireDate("9999-12-31");

		personUsers.add(mailUser);
	}

	private void inflateUserFromCoreDataUser(User user, CoreDataEntry coreDataUser) {
		user.setUserType(USERTYPE_AZUREAD);
		user.setMaster(configuration.getMasterIdentifier());
		user.setMasterId(coreDataUser.getUuid());
		user.setUserId(coreDataUser.getUserId());
	}

	private Map<String, List<CoreDataEntry>> buildCoreDataUserHashMap(Collection<CoreDataEntry> coreDataUsers) {
		return coreDataUsers.stream().collect(Collectors.groupingBy(CoreDataEntry::getCpr));
	}

	private HashMap<String, Person> buildPersonHashMap(Collection<Person> persons) {
		HashMap<String, Person> result = new HashMap<>();
		for (Person person : persons) {
			result.put(person.getCpr(), person);
		}

		return result;
	}
}