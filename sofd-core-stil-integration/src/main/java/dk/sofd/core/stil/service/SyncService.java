package dk.sofd.core.stil.service;

import dk.sofd.core.stil.dao.model.Municipality;
import dk.sofd.core.stil.service.model.Person;
import dk.sofd.core.stil.service.model.Post;
import dk.sofd.core.stil.service.model.StilPerson;
import dk.sofd.core.stil.service.model.User;
import dk.sofd.core.stil.service.stil.IStilService;
import https.wsieksport_unilogin_dk.eksport.fullmyndighed.InstitutionPersonFullMyndighed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SyncService {

	@Autowired
	private SOFDOrganizationService sofdOrganizationService;

	@Autowired
	private IStilService stilService;

	@Autowired
	private MunicipalityService municipalityService;

	@Value("${stil.master.identifier:STIL}")
	private String stilMasterIdentifier;

	@Transactional
	public void sync() {
		for (Municipality municipality : municipalityService.findAll()) {
			if (municipality.isEnabled()) {
				syncMunicipality(municipality);
			} else {
				log.warn("Skipping municipality " + municipality.getName() + " because it is not enabled");
			}

		}
	}

	@Async
	private void syncMunicipality(Municipality municipality) {
		try {
			Collection<Person> sofdPersons = sofdOrganizationService.getPersons(municipality);
			List<StilPerson> stilPersons = new ArrayList<>();

			for (String code : municipality.getStilInstitutions()) {
				log.info("Reading users from institution " + code + " for " + municipality.getName());

				var institution = stilService.getInstitution(code, municipality);
				if (institution == null) {
					continue;
				}

				for (var person : institution.getInstitutionPerson()) {
					// ignore if there is no unilogin object or if cpr contains anything besides digits (ie. test persons)
					if (person.getUNILogin() == null || (person.getUNILogin() != null && !person.getUNILogin().getCivilRegistrationNumber().matches("^\\d{10}$"))) {
						continue;
					}
					if (person.getEmployee() != null || (person.getExtern() != null && municipality.isExternalEnabled() )) {

						// ignore users that we already got from other institutions
						if (stilPersons.stream().filter(p -> p.getUniLogin().equals(person.getUNILogin().getUserId())).count() == 0) {
							var groupPatterns = municipality.getGroupPatterns().stream().map(Pattern::compile).collect(Collectors.toList());
							stilPersons.add(map(person, institution.getInstitutionName(), institution.getInstitutionNumber(), municipality.getEmailSuffix(), groupPatterns));
						} else {
							log.debug("Skipping duplicate user: " + person.getUNILogin().getUserId() + " for " + municipality.getName());
						}
					}
				}
			}

			log.info("Synchronizing " + stilPersons.size() + " users from STIL for " + municipality.getName());
			if (stilPersons.size() < 100) {
				throw new Exception("Something is rotten - got less than 100 persons in stil with a uniLogin.userId");
			}

			fullSync(stilPersons, sofdPersons, municipality);
		} catch (Exception ex) {
			log.error("Synchronization failed for " + municipality.getName(), ex);
		}
	}

	private StilPerson map(InstitutionPersonFullMyndighed person, String institutionName, String institutionNumber, String emailSuffix, List<Pattern> groupPatterns) {
		try {
			StilPerson stilPerson = new StilPerson();

			stilPerson.setCpr(person.getUNILogin().getCivilRegistrationNumber());
			stilPerson.setUniLogin(person.getUNILogin().getUserId());
			stilPerson.setEmail(person.getUNILogin().getUserId() + emailSuffix);
			stilPerson.setInstitutionName(institutionName);
			stilPerson.setInstitutionNumber(institutionNumber);

			if (person.getPerson() != null) {
				stilPerson.setFirstname(person.getPerson().getFirstName());
				stilPerson.setSurname(person.getPerson().getFamilyName());

				if (person.getPerson().getAddress() != null) {
					stilPerson.setStreetAddress(person.getPerson().getAddress().getStreetAddress());
					stilPerson.setCity(person.getPerson().getAddress().getPostalDistrict());
					if (person.getPerson().getAddress().getPostalCode() != null) {
						stilPerson.setPostalCode(Long.parseLong(person.getPerson().getAddress().getPostalCode()));
					}
				}
			} else {
				String name = person.getUNILogin().getName();
				int idx = name.lastIndexOf(' ');
				if (idx > 0 && idx < (name.length() - 1)) {
					stilPerson.setFirstname(name.substring(0, idx));
					stilPerson.setSurname(name.substring(idx + 1));
				} else {
					stilPerson.setFirstname(name);
					stilPerson.setSurname("");
				}
			}

			if (person.getEmployee() != null) {
				stilPerson.setOccupation(person.getEmployee().getOccupation());
				if( person.getEmployee().getRole() != null) {
					var roles = person.getEmployee().getRole().stream().map(r -> r.value()).collect(Collectors.joining(","));
					stilPerson.setRoles(roles);
				}

				if( person.getEmployee().getGroupId() != null) {
					var groups = person.getEmployee().getGroupId().stream().filter(g -> groupPatterns.stream().anyMatch(gp -> gp.matcher(g).matches()) ).collect(Collectors.joining(","));
					stilPerson.setGroups(groups);
				}

			}

			return stilPerson;
		} catch (Exception ex) {
			log.warn("Failed on : " + person.getUNILogin().getUserId());
			throw ex;
		}
	}

	private void fullSync(Collection<StilPerson> stilPersons, Collection<Person> sofdPersons, Municipality municipality) throws Exception {
		merge(buildStilPersonsHashMap(stilPersons), buildPersonHashMap(sofdPersons), municipality);
	}

	private void merge(HashMap<String, StilPerson> stilPersons, HashMap<String, Person> persons, Municipality municipality) throws Exception {
		long createCount = 0, updateCount = 0, deleteCount = 0;

		// find person elements that need to be created. ie. elements that are not in sofd organization
		HashSet<String> toBeCreated = new HashSet<>(stilPersons.keySet());
		toBeCreated.removeAll(persons.keySet());
		for (String cpr : toBeCreated) {
			handleCreate(stilPersons.get(cpr), municipality);
			createCount++;
		}

		// find person elements that need to be updated. ie. the intersection of both sets
		HashSet<String> toBeUpdated = new HashSet<>(stilPersons.keySet());
		toBeUpdated.retainAll(persons.keySet());
		for (String cpr : toBeUpdated) {
			if (handleUpdate(stilPersons.get(cpr), persons.get(cpr), municipality)) {
				updateCount++;
			}
		}

		// find person elements that need to be deleted. ie. elements that are not in STIL
		HashSet<String> toBeDeleted = new HashSet<>(persons.keySet());
		toBeDeleted.removeAll(stilPersons.keySet());
		for (String cpr : toBeDeleted) {
			if (handleDelete(persons.get(cpr), municipality)) {
				deleteCount++;
			}
		}

		log.info("Created " + createCount + ", Updated " + updateCount + ", Deleted " + deleteCount + " for " + municipality.getName());
	}

	private void handleCreate(StilPerson stilPerson, Municipality municipality) throws Exception {
		Person person = new Person();
		person.setMaster(stilMasterIdentifier);
		person.setCpr(stilPerson.getCpr());
		person.setFirstname(stilPerson.getFirstname());
		person.setSurname(stilPerson.getSurname());
		person.setRegisteredPostAddress(getPostFromStilPerson(stilPerson));
		person.setUsers(new HashSet<>());

		User user = new User();
		user.setUuid(UUID.randomUUID().toString());
		inflateUserFromStilPerson(user, stilPerson);
		person.getUsers().add(user);

		if (municipality.isEnableEmail()) {
			addEmailToPerson(person.getUsers(), stilPerson, user.getUuid());
		}

		sofdOrganizationService.create(person, municipality);
	}

	private Post getPostFromStilPerson(StilPerson stilPerson) {
		if (stilPerson.getStreetAddress() != null && stilPerson.getStreetAddress().length() > 0) {
			Post post = new Post();
			post.setAddressProtected(false);
			post.setMaster(stilMasterIdentifier);
			post.setMasterId(stilPerson.getUniLogin());
			post.setStreet(stilPerson.getStreetAddress());
			post.setPostalCode(Long.toString(stilPerson.getPostalCode()));
			post.setCity(stilPerson.getCity());
			post.setCountry("DK");

			return post;
		}

		return null;
	}

	private boolean handleUpdate(StilPerson stilPerson, Person person, Municipality municipality) throws Exception {
		Person patchedPerson = new Person();
		boolean shouldUpdate = false;

		// if the person is deleted, revive it and take control of it
		if (person.isDeleted()) {
			shouldUpdate = true;
			patchedPerson.setMaster(stilMasterIdentifier);
			patchedPerson.setDeleted(false); // not a big fan of middleware deciding to undelete persons, but it saves waiting for the next nightly run
		}

		Set<User> users = person.getUsers();

		List<User> uniLoginUsers = users.stream()
				.filter(u -> u.getMaster().equalsIgnoreCase(stilMasterIdentifier) && u.getUserType().equals("UNILOGIN"))
				.collect(Collectors.toList());

		List<User> mailUsers = users.stream()
				.filter(u -> u.getMaster().equalsIgnoreCase(stilMasterIdentifier) && u.getUserType().equals("SCHOOL_EMAIL"))
				.collect(Collectors.toList());

		String sofdUserUuid = null;
		for (User user : uniLoginUsers) {
			if (user.getMasterId().equalsIgnoreCase(stilPerson.getUniLogin())) {

				// found a matching user. We should update it
				sofdUserUuid = user.getUuid();

				// create a copy of the user object
				User originalUser = user.toBuilder().build();
				inflateUserFromStilPerson(user, stilPerson);

				// patch if user differs from original
				if (!user.equals(originalUser)) {
					shouldUpdate = true;

					patchedPerson.setUsers(users);
				}
			} else {
				// found another user created by this master. We don't allow
				// multiple STIL users per person so we should delete it
				users.remove(user);
				patchedPerson.setUsers(users);

				shouldUpdate = true;
			}
		}

		if (sofdUserUuid == null) {
			// no corresponding user was found. We should add one
			User user = new User();
			user.setUuid(UUID.randomUUID().toString());
			inflateUserFromStilPerson(user, stilPerson);

			users.add(user);
			patchedPerson.setUsers(users);

			if (municipality.isEnableEmail()) {
				addEmailToPerson(patchedPerson.getUsers(), stilPerson, user.getUuid());
			}

			shouldUpdate = true;
		} else {
			if (municipality.isEnableEmail()) {
				boolean foundExistingMailUser = false;

				// if we updated an existing user, we should also (potentially) update the email user
				for (User mailUser : mailUsers) {

					// see if we can find a mail account that matches
					if (mailUser.getMasterId().equalsIgnoreCase(sofdUserUuid)) {
						foundExistingMailUser = true;

						if (!Objects.equals(mailUser.getUserId(), stilPerson.getEmail())) {
							mailUser.setUserId(stilPerson.getEmail());

							patchedPerson.setUsers(users);
							shouldUpdate = true;
						}
					} else {
						// only one mail account is allowed from STIL, so remove it
						users.remove(mailUser);

						patchedPerson.setUsers(users);
						shouldUpdate = true;
					}
				}

				if (!foundExistingMailUser) {
					// if no existing mail user was found, attempt to add a mail user (if one exists within STIL data)
					if (addEmailToPerson(users, stilPerson, sofdUserUuid)) {
						patchedPerson.setUsers(users);
						shouldUpdate = true;
					}
				}
			}
		}

		if (shouldUpdate) {
			patchedPerson.setUuid(person.getUuid());
			sofdOrganizationService.update(patchedPerson, municipality);
		}

		return shouldUpdate;
	}

	private boolean addEmailToPerson(Set<User> personUsers, StilPerson stilPerson, String userUuid) {
		if (stilPerson.getEmail() != null && stilPerson.getEmail().length() > 0) {
			User mailUser = new User();

			mailUser.setUserType("SCHOOL_EMAIL");
			mailUser.setUuid(UUID.randomUUID().toString());
			mailUser.setMaster(stilMasterIdentifier);
			mailUser.setMasterId(userUuid);
			mailUser.setUserId(stilPerson.getEmail());
			mailUser.setAccountExpireDate("9999-12-31");
			mailUser.setPasswordExpireDate("9999-12-31");

			personUsers.add(mailUser);

			return true;
		}

		return false;
	}

	private User inflateUserFromStilPerson(User user, StilPerson stilPerson) {
		user.setUserType("UNILOGIN");
		user.setMaster(stilMasterIdentifier);
		user.setMasterId(stilPerson.getUniLogin());
		user.setUserId(stilPerson.getUniLogin());
		user.setAccountExpireDate("9999-12-31");
		user.setPasswordExpireDate("9999-12-31");

		Map<String, String> extensions = new HashMap<>();
		extensions.put("institution", stilPerson.getInstitutionName());
		extensions.put("institutionCode", stilPerson.getInstitutionNumber());
		if (stilPerson.getOccupation() != null) {
			extensions.put("occupation", stilPerson.getOccupation());
		}
		if( stilPerson.getRoles() != null) {
			extensions.put("roles", stilPerson.getRoles());
		}
		if( stilPerson.getGroups() != null) {
			extensions.put("groups", stilPerson.getGroups());
		}

		user.setLocalExtensions(extensions);

		return user;
	}

	private boolean handleDelete(Person person, Municipality municipality) throws Exception {
		Person patchedPerson = new Person();
		patchedPerson.setUuid(person.getUuid());
		boolean shouldUpdate = false;

		// remove all STIL users from this person
		HashSet<User> nonSTILUsers = new HashSet<User>();
		for (User user : person.getUsers()) {
			if (user.getMaster().equalsIgnoreCase(stilMasterIdentifier)) {
				shouldUpdate = true;
			} else {
				nonSTILUsers.add(user);
			}
		}

		patchedPerson.setUsers(nonSTILUsers);

		if (shouldUpdate) {
			sofdOrganizationService.update(patchedPerson, municipality);
		}

		return shouldUpdate;
	}

	private HashMap<String, StilPerson> buildStilPersonsHashMap(Collection<StilPerson> stilPersons) {
		HashMap<String, StilPerson> result = new HashMap<String, StilPerson>();
		for (StilPerson stilUser : stilPersons) {
			result.put(stilUser.getCpr(), stilUser);
		}

		return result;
	}

	private HashMap<String, Person> buildPersonHashMap(Collection<Person> persons) {
		HashMap<String, Person> result = new HashMap<String, Person>();
		for (Person person : persons) {
			result.put(person.getCpr(), person);
		}
		return result;
	}
}
