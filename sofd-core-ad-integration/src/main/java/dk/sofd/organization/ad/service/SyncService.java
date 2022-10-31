package dk.sofd.organization.ad.service;

import static dk.sofd.organization.ad.utility.NullChecker.getValue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.graph.requests.GraphServiceClient;
import dk.sofd.organization.ad.service.model.*;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dk.sofd.organization.ad.activedirectory.ADUser;
import dk.sofd.organization.ad.security.MunicipalityHolder;
import dk.sofd.organization.ad.service.model.enums.Visibility;
import dk.sofd.organization.ad.utility.ObjectCloner;
import dk.sofd.organization.ad.dao.model.Municipality;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Component
public class SyncService {
	private static final String adMasterIdentifier = "ActiveDirectory";

	@Autowired
	private SOFDOrganizationService sofdOrganizationService;

	@Autowired
	private AzureAdService azureAdService;

	@Async
	public void fullSync(List<ADUser> adUsers, Municipality municipality) throws Exception {
		MunicipalityHolder.set(municipality);

		log.info("Starting full sync for: " + municipality.getName());

		try {
			if (municipality.isAzureLookupEnabled()) {
				Map<String, AzureUser> azureUsers = azureAdService.fetchAllAzureUsers(municipality);
				enrichADUsersWithAzureADLocalExtension(adUsers, azureUsers, municipality);
			}

			merge(municipality, buildADUserHashMap(adUsers), buildPersonHashMap(sofdOrganizationService.getPersons()), true);

			log.info("Completed full sync for: " + municipality.getName());
		}
		catch (Exception ex) {
			log.error("Failed to perform full sync for: " + municipality.getName(), ex);
		}
	}

	@Async
	public void deltaSync(List<ADUser> adUsers, Municipality municipality) throws Exception {
		MunicipalityHolder.set(municipality);

		log.info("Starting delta sync for: " + municipality.getName());

		try {
			boolean doFullFetchOfAzure = adUsers.size() > 10;

			Map<String, AzureUser> azureUsers = new HashMap<>();
			GraphServiceClient<Request> graphServiceClient = null;
			if (municipality.isAzureLookupEnabled()) {
				// Use one graph client for one round of delta requests
				graphServiceClient = azureAdService.initializeGraphAuth(municipality);

				if (doFullFetchOfAzure) {
					azureUsers = azureAdService.fetchAllAzureUsers(municipality);
				}
			}

			for (ADUser adUser : adUsers) {
				if( adUser.isDeleted() ) {
					sofdOrganizationService.deleteUserByADMasterId(adUser.getObjectGuid());
				}
				// only perform synchronization if we can map user to a person by cpr
				else if (adUser.hasValidCprAttribute()) {
					if (municipality.isAzureLookupEnabled()) {
						if (!doFullFetchOfAzure) {
							Map.Entry<String, AzureUser> azureUserEntry = azureAdService.fetchAzureUserById(graphServiceClient, municipality, adUser.getUserId());
							if (azureUserEntry != null) {
								azureUsers.put(azureUserEntry.getKey(), azureUserEntry.getValue());
							}
						}

						enrichAdUser(azureUsers, municipality, adUser);
					}

					Collection<Person> persons = sofdOrganizationService.getPersons(adUser.getCpr());
					merge(municipality, buildADUserHashMap(Arrays.asList(adUser)), buildPersonHashMap(persons), false);
				}
				else {
					log.warn(adUser.getUserId() + " does not have a valid cpr '" + adUser.getCpr() + "'");
				}
			}

			log.info("Completed delta sync for: " + municipality.getName());
		}
		catch (Exception ex) {
			log.error("Failed to perform delta sync for: " + municipality.getName(), ex);
		}
	}

	private void enrichADUsersWithAzureADLocalExtension(List<ADUser> adUsers, Map<String, AzureUser> azureUsers, Municipality municipality) {
		// add local extension to all AD users that match a user in Azure AD
		if (azureUsers != null) {
			for (ADUser adUser : adUsers) {
				enrichAdUser(azureUsers, municipality, adUser);
			}
		}
	}

	private void enrichAdUser(Map<String, AzureUser> azureUsers, Municipality municipality, ADUser adUser) {
		if (azureUsers.containsKey(adUser.getUserId().toLowerCase())) {
			if (adUser.getLocalExtensions() == null) {
				adUser.setLocalExtensions(new HashMap<>());
			}

			adUser.getLocalExtensions().put("AzureId", azureUsers.get(adUser.getUserId().toLowerCase()).getId());
			adUser.getLocalExtensions().put("AzureTenantId", municipality.getAzureTenantId());
		}
	}

	private void merge(Municipality municipality, HashMap<String, List<ADUser>> adUsers, HashMap<String, Person> persons, boolean isFullSync) throws Exception {
		if (StringUtils.hasLength(municipality.getNameReplacePattern())) {
			regexReplaceChosenName(municipality, adUsers);
		}

		// find person elements that need to be created. ie. elements that are
		// not in sofd organization
		HashSet<String> toBeCreated = new HashSet<>(adUsers.keySet());
		toBeCreated.removeAll(persons.keySet());
		for (String cpr : toBeCreated) {
			if (adUsers.get(cpr).stream().anyMatch(u -> u.shouldSynchronizeUser(municipality.isSupportInactiveUsers()))) {
				handleCreate(municipality, adUsers.get(cpr));
			}
		}

		// find person elements that need to be updated. ie. the intersection of
		// both sets
		HashSet<String> toBeUpdated = new HashSet<>(adUsers.keySet());
		toBeUpdated.retainAll(persons.keySet());
		for (String cpr : toBeUpdated) {
			handleUpdate(municipality, adUsers.get(cpr), persons.get(cpr), isFullSync);
		}

		// find person elements that need to be deleted. ie. elements that are
		// not in active directory
		HashSet<String> toBeDeleted = new HashSet<>(persons.keySet());
		toBeDeleted.removeAll(adUsers.keySet());
		for (String cpr : toBeDeleted) {
			handleDelete(municipality, persons.get(cpr));
		}
	}

	private void regexReplaceChosenName(Municipality municipality, HashMap<String, List<ADUser>> adUsers) {
		for (List<ADUser> adUsersList : adUsers.values()) {
			for (ADUser adUser : adUsersList) {
				if (adUser.getChosenName() != null) {
					adUser.setChosenName(adUser.getChosenName().replaceAll(municipality.getNameReplacePattern(), municipality.getNameReplaceValue() == null ? "" : municipality.getNameReplaceValue()));
				}
			}
		}
	}

	private void handleCreate(Municipality municipality, List<ADUser> adUsers) throws Exception {
		ADUser firstADUser = adUsers.stream().sorted().findFirst().get();
		Person person = new Person();
		person.setMaster(adMasterIdentifier);
		person.setCpr(firstADUser.getCpr());
		person.setFirstname(firstADUser.getFirstname());
		person.setSurname(firstADUser.getSurname());
		person.setChosenName(firstADUser.getChosenName() != null ? firstADUser.getChosenName() : null);
		person.setPhones(getPhones(firstADUser));

		// fix firstname/surname
		if (!StringUtils.hasLength(person.getFirstname())) {
			person.setFirstname("Ukendt");
		}
		if (!StringUtils.hasLength(person.getSurname())) {
			person.setSurname("Ukendtsen");
		}

		HashSet<User> users = new HashSet<User>();
		HashSet<Affiliation> affiliations = new HashSet<Affiliation>();
		for (ADUser adUser : adUsers.stream().filter(u -> u.shouldSynchronizeUser(municipality.isSupportInactiveUsers())).collect(Collectors.toList())) {
			// User has an affiliation registered in AD (external users, substitutes etc.)
			// TODO: remove this feature some point in the future... Næstved, Favrskov and Syddjurs (maybe Norddjurs) uses
			//       this, but they should start using SOFD for registering this instead
			if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
				if (adUser.getAffiliation() != null) {
					Affiliation affiliation = new Affiliation();
					affiliation.setUuid(UUID.randomUUID().toString());
					inflateAffiliationFromAdUser(affiliation, adUser);

					if (affiliation.getOrgUnitUuid() != null) {
						affiliations.add(affiliation);
					}
				}
			}

			User user = new User();
			user.setUuid(UUID.randomUUID().toString());
			inflateUserFromAdUser(municipality, user, adUser);
			users.add(user);

			// from the REAL active directory, we also accept exchange addresses
			if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
				// if the AD user has an email address, we also create an Exchange user account
				if (adUser.getEmail() != null) {
					users.add(inflateExchangeUserFromAdUser(user, adUser));
				}
			}
		}

		person.setAffiliations(affiliations);
		person.setUsers(users);

		var createdPerson = sofdOrganizationService.create(person);
		if (createdPerson != null && municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
			patchPhoto(createdPerson.getUuid(), firstADUser.getPhoto());
		}
	}

	private User inflateExchangeUserFromAdUser(User user, ADUser adUser) {
		User exchangeUser = new User();
		exchangeUser.setUuid(UUID.randomUUID().toString());
		exchangeUser.setMaster(adMasterIdentifier);
		exchangeUser.setMasterId(user.getUserId());
		exchangeUser.setUserId(adUser.getEmail());
		exchangeUser.setUserType("EXCHANGE");
		exchangeUser.setDisabled(adUser.getDisabled());

		return exchangeUser;
	}

	// we will try how this plays out with Galera/MariaDB
	private static boolean isLatin1(String v) {
//		return Charset.forName("ISO-8859-1").newEncoder().canEncode(v);
		return true;
	}

	private void handleUpdate(Municipality municipality, List<ADUser> adUsers, Person person, boolean isFullSync) throws Exception {
		Person patchedPerson = new Person();
		boolean hasActiveUsers = adUsers.stream().anyMatch(u -> u.shouldSynchronizeUser(false));

		// undelete deleted Persons if we have active AD accounts (and take control of the Person if needed)
		if (hasActiveUsers && person.isDeleted()) {
			patchedPerson.setMaster(adMasterIdentifier);
			patchedPerson.setDeleted(false); // not a big fan of our middleware undeleting, but it saves waiting for the next nightly run
		}

		// phones, chosenName and photo are only copied from the prime AD user
		ADUser primeAdUser = getPrimeADUser(municipality, adUsers, person);

		byte[] photo = null;
		if (primeAdUser != null) {

			// MySQLs latin1 charset does not play well with special characters, so we replace them before comparing
			String chosenName = (primeAdUser.getChosenName() != null) ? primeAdUser.getChosenName() : "";
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < chosenName.length(); i++) {
				String val = chosenName.substring(i, i + 1);
				builder.append(isLatin1(val) ? val : "?");
			}
			chosenName = builder.toString();

			if (!getValue(() -> person.getChosenName(), "").equals(chosenName)) {
				if (StringUtils.hasLength(chosenName)) {
					patchedPerson.setChosenName(chosenName);
				}
				else {
					patchedPerson.setChosenName("");
				}
			}

			Set<Phone> adPhones = getPhones(primeAdUser);
			if (person.getPhones() == null || person.getPhones().size() == 0) {
				// only actually set if there are phones, otherwise the check to see if we should
				// call the backend will not work
				if (adPhones.size() > 0) {
					patchedPerson.setPhones(adPhones);
				}
			}
			else { // we know that there is at least one phone on the Person already
				Set<Phone> patchedPhones = new HashSet<>(person.getPhones());
				boolean changes = false;

				// find those to remove (if any)
				for (Phone phone : person.getPhones()) {
					if (phone.getMaster().equals(adMasterIdentifier)) {
						boolean found = false;

						for (Phone adPhone : adPhones) {
							if (adPhone.getMasterId().equals(phone.getMasterId())) {
								found = true;
								break;
							}
						}

						if (!found) {
							patchedPhones = patchedPhones.stream()
									.filter(p -> !(p.getMaster().equals(adMasterIdentifier) & p.getMasterId().equals(phone.getMasterId())))
									.collect(Collectors.toSet());

							changes = true;
						}
					}
				}

				// find those to add (if any)
				for (Phone adPhone : adPhones) {
					boolean found = false;

					for (Phone phone : person.getPhones()) {
						if (!phone.getMaster().equals(adMasterIdentifier) || !phone.getMasterId().equals(adPhone.getMasterId())) {
							continue;
						}

						found = true;
						break;
					}

					if (!found) {
						patchedPhones.add(adPhone);
						changes = true;
					}
				}

				if (changes) {
					patchedPerson.setPhones(patchedPhones);
				}
			}

			photo = primeAdUser.getPhoto();
		}

		// TODO: we want to deprecate this feature, but for now we just ensure
		//       that only the REAL active directory gets to set these values
		// Handle affiliations
		if ("ACTIVE_DIRECTORY".equals(municipality.getUserType())) {
			handleAffiliations(municipality, adUsers, person, patchedPerson, isFullSync);
		}

		// Handle users
		handleUsers(municipality, adUsers, person, patchedPerson, isFullSync);

		// an update is required if the patched person differs from a new empty
		if (!patchedPerson.equals(new Person())) {
			patchedPerson.setUuid(person.getUuid());

			sofdOrganizationService.update(patchedPerson);
		}

		if ("ACTIVE_DIRECTORY".equals(municipality.getUserType())) {
			patchPhoto(person.getUuid(), photo);
		}
	}

	private void patchPhoto(String personUuid, byte[] photo) {
		if (photo == null) {
			// no photo property sent from dispatcher (photo was not changed) - do nothing
			return;
		}

		if (photo != null && photo.length == 0) {
			// photo was deleted in AD
			sofdOrganizationService.deletePhoto(personUuid);
		}
		else {
			// photo was updated in AD
			sofdOrganizationService.postPhoto(personUuid, photo);
		}
	}

	// this method, on purpose, returns the prime AD user, and not the prime municipality.userType() user,
	// the reason being that we use the prime AD user for setting the displayName, phones, etc on the user,
	// and we do not want multiple AD's to battle for control here
	private ADUser getPrimeADUser(Municipality municipality, List<ADUser> adUsers, Person person) {
		if (!municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
			return null;
		}

		Optional<User> sofdPrimeUserOptional = person.getUsers().stream()
				.filter(sofdUser -> sofdUser.getMaster().equals(adMasterIdentifier) &&
									sofdUser.getUserType().equals(municipality.getUserType()) &&
						            sofdUser.isPrime())
				.findFirst();

		if (sofdPrimeUserOptional.isPresent()) {
			User sofdPrimeUser = sofdPrimeUserOptional.get();

			for (ADUser adUser : adUsers) {
				if (sofdPrimeUser.getMasterId().equals(municipality.getMasterIdPrefix() + adUser.getObjectGuid())) {
					return adUser;
				}
			}
		}

		return null;
	}

	private void handleUsers(Municipality municipality, List<ADUser> adUsers, Person person, Person patchedPerson, boolean isFullSync) {
		ObjectCloner objectCloner = new ObjectCloner();
		Set<User> sofdUsers = person.getUsers();
		boolean hasUsersChanged = false;

		for (ADUser adUser : adUsers) {
			Optional<User> matchingSofdUser = sofdUsers.stream()
					.filter(u -> u.getMaster().equals(adMasterIdentifier) &&
							     u.getUserType().equals(municipality.getUserType()) &&
							     u.getMasterId().equals(municipality.getMasterIdPrefix() + adUser.getObjectGuid()))
					.findFirst();

			if (matchingSofdUser.isPresent()) {
				User sofdUser = matchingSofdUser.get();

				Optional<User> matchingEmailUser = sofdUsers.stream()
						.filter(u -> u.getMaster().equals(adMasterIdentifier) &&
								     u.getUserType().equals("EXCHANGE") &&
								     u.getMasterId().equals(sofdUser.getUserId()))
						.findFirst();

				// email users are only for real active directory
				User emailUser = null;
				if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
					emailUser = matchingEmailUser.isPresent() ? matchingEmailUser.get() : null;
				}

				if (!adUser.shouldSynchronizeUser(municipality.isSupportInactiveUsers())) {
					sofdUsers.remove(sofdUser);
					if (emailUser != null) {
						sofdUsers.remove(emailUser);
					}

					hasUsersChanged = true;
				}
				else {
					User originalUser = objectCloner.deepCopy(sofdUser);
					inflateUserFromAdUser(municipality, sofdUser, adUser);

					if (!sofdUser.equals(originalUser)) {
						hasUsersChanged = true;
					}

					// update/create derived EXCHANGE user as well (for real AD only)
					if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
						if (adUser.getEmail() != null) {
							if (emailUser != null) {
								if (!Objects.equals(emailUser.getUserId(), adUser.getEmail()) ||
									!Objects.equals(emailUser.getDisabled(), adUser.getDisabled())) {
									emailUser.setUserId(adUser.getEmail());
									emailUser.setDisabled(adUser.getDisabled());

									hasUsersChanged = true;
								}
							}
							else {
								sofdUsers.add(inflateExchangeUserFromAdUser(sofdUser, adUser));

								hasUsersChanged = true;
							}
						}
						else {
							if (emailUser != null) {
								sofdUsers.remove(emailUser);

								hasUsersChanged = true;
							}
						}
					}
				}
			}
			else if (adUser.shouldSynchronizeUser(municipality.isSupportInactiveUsers())) {

				// no corresponding user was found. We should add one
				User user = new User();
				user.setUuid(UUID.randomUUID().toString());
				inflateUserFromAdUser(municipality, user, adUser);

				sofdUsers.add(user);

				if (municipality.getUserType().equals("ACTIVE_DIRECTORY") && adUser.getEmail() != null) {
					sofdUsers.add(inflateExchangeUserFromAdUser(user, adUser));
				}

				hasUsersChanged = true;
			}
		}

		if (isFullSync) {

			// filter any ACTIVE_DIRECTORY users created by this master that are no longer present
			List<User> oldADUsersInSofd = sofdUsers.stream()
					.filter(sofdUser -> sofdUser.getMaster().equals(adMasterIdentifier)
							&& sofdUser.getUserType().equals(municipality.getUserType())
							&& adUsers.stream().noneMatch(adUser -> (municipality.getMasterIdPrefix() + adUser.getObjectGuid()).equals(sofdUser.getMasterId())))
					.collect(Collectors.toList());

			// filter any EXCHANGE users created by this master that are no longer present
			List<User> oldExchangeUsersInSofd = new ArrayList<User>();
			if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
				oldExchangeUsersInSofd = sofdUsers.stream()
						.filter(sofdUser -> sofdUser.getMaster().equals(adMasterIdentifier)
								&& sofdUser.getUserType().equals("EXCHANGE")
								&& adUsers.stream().noneMatch(adUser -> adUser.getUserId().equals(sofdUser.getMasterId())))
						.collect(Collectors.toList());
			}

			// delete old ACTIVE_DIRECTORY accounts
			for (User oldADUserInSofd : oldADUsersInSofd) {
				sofdUsers.remove(oldADUserInSofd);
				hasUsersChanged = true;
			}

			// delete old EXCHANGE accounts
			for (User oldExchangeUserInSofd : oldExchangeUsersInSofd) {
				sofdUsers.remove(oldExchangeUserInSofd);
				hasUsersChanged = true;
			}
		}

		if (hasUsersChanged) {
			patchedPerson.setUsers(sofdUsers);
		}
	}

	private void handleAffiliations(Municipality municipality, List<ADUser> adUsers, Person person, Person patchedPerson, boolean isFullSync) {
		ObjectCloner objectCloner = new ObjectCloner();
		Set<Affiliation> sofdAffiliations = person.getAffiliations();
		boolean hasAffiliationsChanged = false;
        LocalDate now = toLocalDate(new Date());

		for (ADUser adUser : adUsers.stream().filter(u -> u.shouldSynchronizeUser(municipality.isSupportInactiveUsers())).collect(Collectors.toList())) {
			// if there is a corresponding (i.e. same orgunit) affiliation owned by another master,
			// we will ignore the AD owned affiliation, as it is likely leftover data in AD, that
			// should have been cleaned up
			if (adUser.getAffiliation() != null) {
				String orgUnitUuid = sofdOrganizationService.getOrgUnitUuidByMasterId(adUser.getAffiliation());

				// any other affiliation that points to same OU, which is not _this_ affilation
				if (orgUnitUuid != null && sofdAffiliations.stream().anyMatch(a ->
					orgUnitUuid.equals(a.getOrgUnitUuid()) &&
					!a.isDeleted() &&
					(a.getStopDate() == null || (a.getStopDate().length() >= 10 && LocalDate.parse(a.getStopDate().substring(0, 10)).isAfter(now))) &&
					!a.getMasterId().equals(adUser.getObjectGuid()) &&
					!a.getMaster().equals(adMasterIdentifier))) {

					// set to null, so the logic below will ignore this affiliation
					adUser.setAffiliation(null);
				}
			}

			// find any matching affiliations in sofd
			if (adUser.getAffiliation() != null) {
				Optional<Affiliation> matchingSofdAffiliation = sofdAffiliations.stream()
						.filter(sofdAffiliation -> sofdAffiliation.getMaster().equals(adMasterIdentifier)
								&& sofdAffiliation.getMasterId().equals(adUser.getObjectGuid()))
						.findFirst();

				if (matchingSofdAffiliation.isPresent()) {
					Affiliation originalAffiliation = objectCloner.deepCopy(matchingSofdAffiliation.get());
					inflateAffiliationFromAdUser(matchingSofdAffiliation.get(), adUser);

					if (!matchingSofdAffiliation.get().equals(originalAffiliation)) {
						hasAffiliationsChanged = true;
					}
				}
				else {
					// no corresponding affiliation was found. We should add one
					Affiliation affiliation = new Affiliation();
					affiliation.setUuid(UUID.randomUUID().toString());
					inflateAffiliationFromAdUser(affiliation, adUser);

					if (affiliation.getOrgUnitUuid() != null) {
						sofdAffiliations.add(affiliation);
						hasAffiliationsChanged = true;
					}
				}
			}
		}

		// find any affiliations created by this master that is no
		// longer present and delete them from sofd
		List<Affiliation> oldAffiliationsInSofd = sofdAffiliations.stream()
				.filter(sofdAffiliation -> sofdAffiliation.getMaster().equals(adMasterIdentifier)
						&& !sofdAffiliation.isDeleted()
						&& adUsers.stream().noneMatch(adUser -> adUser.shouldSynchronizeUser(municipality.isSupportInactiveUsers()) && adUser.getAffiliation() != null && adUser.getObjectGuid().equals(sofdAffiliation.getMasterId())))
				.collect(Collectors.toList());

		for (Affiliation oldAffiliationInSofd : oldAffiliationsInSofd) {
			// only delete other users if it is a full sync, otherwise only delete affiliation if it was removed from any of the delta users
			if( isFullSync || adUsers.stream().anyMatch( adUser -> adUser.getObjectGuid().equals(oldAffiliationInSofd.getMasterId())))
			{
				oldAffiliationInSofd.setDeleted(true);
				hasAffiliationsChanged = true;
			}
		}

		if (hasAffiliationsChanged) {
			patchedPerson.setAffiliations(sofdAffiliations);
		}
	}

	private User inflateUserFromAdUser(Municipality municipality, User user, ADUser adUser) {
		user.setUserType(municipality.getUserType());
		user.setMaster(adMasterIdentifier);
		user.setMasterId(municipality.getMasterIdPrefix() + adUser.getObjectGuid());
		user.setUserId(adUser.getUserId());
		user.setLocalExtensions(adUser.getLocalExtensions());
		user.setDisabled(adUser.getDisabled());
		user.setPasswordLocked(adUser.isPasswordLocked());

		if (adUser.getDaysToPwdChange() >= 10000) {
			// will never expire
			user.setPasswordExpireDate("9999-12-31");
		}
		else if (adUser.getDaysToPwdChange() > 0) {
			// not expired yet
			user.setPasswordExpireDate(LocalDate.now().plusDays(adUser.getDaysToPwdChange()).toString());
		}
		else {
			// expired
			user.setPasswordExpireDate("1970-01-01");
		}

		// null just means "unknown", which is ignored by backend (no update in DB, old value is kept)
		if (StringUtils.hasLength(adUser.getAccountExpireDate())) {
			user.setAccountExpireDate(adUser.getAccountExpireDate());
		}

		user.setEmployeeId(adUser.getEmployeeId());
		user.setUpn(adUser.getUpn());

		return user;
	}

	private Affiliation inflateAffiliationFromAdUser(Affiliation affiliation, ADUser adUser) {
		String orgUnitUuid = sofdOrganizationService.getOrgUnitUuidByMasterId(adUser.getAffiliation());
		if (orgUnitUuid == null) {
			log.warn("No OrgUnit that matches LOS ID for ADUser: " + adUser.getUserId() + " / " + adUser.getAffiliation());
			return null;
		}

		affiliation.setAffiliationType("EXTERNAL");
		affiliation.setMaster(adMasterIdentifier);
		affiliation.setMasterId(adUser.getObjectGuid());
		affiliation.setDeleted(false);
		affiliation.setOrgUnitUuid(orgUnitUuid);
		affiliation.setPositionName(getValue(() -> adUser.getTitle(), "Ukendt"));

		return affiliation;
	}

	private void handleDelete(Municipality municipality, Person person) throws Exception {
		Person patchedPerson = new Person();
		patchedPerson.setUuid(person.getUuid());
		boolean shouldUpdate = false;

		// remove all active_directory users from this person
		boolean patchUsers = false;
		Set<User> nonADUsers = new HashSet<User>();
		for (User user : person.getUsers()) {
			if (user.getMaster().equalsIgnoreCase(adMasterIdentifier)) {
				if (user.getUserType().equalsIgnoreCase(municipality.getUserType())) {
					patchUsers = true;
				}
				else if (user.getUserType().equals("EXCHANGE") && "ACTIVE_DIRECTORY".equals(municipality.getUserType())) {
					// special case - if we have a Person in SOFD with an exchange account, but NO AD accounts,
					// then we should always remove them - but only if the userType is "ACTIVE_DIRECTORY" for
					// the municipality (*sigh* - we really need to get rid of that feature)
					patchUsers = true;
				}
				else {
					nonADUsers.add(user);
				}
			}
			else {
				nonADUsers.add(user);
			}
		}

		if (patchUsers) {
			patchedPerson.setUsers(nonADUsers);
			shouldUpdate = true;
		}

		// TODO: we want to deprecate this feature, but for now we just ensure
		//       that only the REAL active directory gets to delete these values
		if ("ACTIVE_DIRECTORY".equalsIgnoreCase(municipality.getUserType())) {
			// remove all active_directory affiliations from this person
			boolean patchAffiliations = false;
			Set<Affiliation> nonADAffiliations = new HashSet<>();
			if (person.getAffiliations() != null) {
				for (Affiliation affiliation : person.getAffiliations()) {
					if (affiliation.getMaster().equals(adMasterIdentifier)) {
						patchAffiliations = true;
					}
					else {
						nonADAffiliations.add(affiliation);
					}
				}
			}

			if (patchAffiliations) {
				patchedPerson.setAffiliations(nonADAffiliations);
				shouldUpdate = true;
			}
		}

		if (shouldUpdate) {
			sofdOrganizationService.update(patchedPerson);
		}
	}

	private Set<Phone> getPhones(ADUser adUser) {
		Set<Phone> phones = new HashSet<>();

		if (StringUtils.hasLength(adUser.getMobile())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("v" + adUser.getMobile()); // visible marker, for unique masterId
			phone.setPhoneNumber(adUser.getMobile());
			phone.setPhoneType("MOBILE");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		if (StringUtils.hasLength(adUser.getSecretMobile())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("h" + adUser.getSecretMobile()); // hidden marker, for unique masterId
			phone.setPhoneNumber(adUser.getSecretMobile());
			phone.setPhoneType("MOBILE");
			phone.setVisibility(Visibility.HIDDEN_FOR_CITIZENS);

			phones.add(phone);
		}

		if (StringUtils.hasLength(adUser.getPhone())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("p" + adUser.getPhone()); // phone marker, for unique masterId
			phone.setPhoneNumber(adUser.getPhone());
			phone.setPhoneType("LANDLINE");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		if (StringUtils.hasLength(adUser.getDepartmentNumber())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("d" + adUser.getPhone()); // department marker, for unique masterId
			phone.setPhoneNumber(adUser.getPhone());
			phone.setPhoneType("DEPARTMENT_NUMBER");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		if (StringUtils.hasLength(adUser.getFaxNumber())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("f" + adUser.getPhone()); // fax marker, for unique masterId
			phone.setPhoneNumber(adUser.getPhone());
			phone.setPhoneType("FAX_NUMBER");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		return phones;
	}

	private HashMap<String, List<ADUser>> buildADUserHashMap(Collection<ADUser> adUsers) {
		HashMap<String, List<ADUser>> result = new HashMap<String, List<ADUser>>();
		for (ADUser adUser : adUsers) {
			if (!result.containsKey(adUser.getCpr())) {
				result.put(adUser.getCpr(), new ArrayList<ADUser>());
			}
			result.get(adUser.getCpr()).add(adUser);
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

	private LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}

	    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
