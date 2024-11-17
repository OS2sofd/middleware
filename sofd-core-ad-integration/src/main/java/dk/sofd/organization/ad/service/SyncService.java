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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.microsoft.graph.requests.GraphServiceClient;

import dk.sofd.organization.ad.activedirectory.ADUser;
import dk.sofd.organization.ad.dao.model.Municipality;
import dk.sofd.organization.ad.security.MunicipalityHolder;
import dk.sofd.organization.ad.service.model.Affiliation;
import dk.sofd.organization.ad.service.model.AzureUser;
import dk.sofd.organization.ad.service.model.Person;
import dk.sofd.organization.ad.service.model.Phone;
import dk.sofd.organization.ad.service.model.User;
import dk.sofd.organization.ad.service.model.enums.Visibility;
import dk.sofd.organization.ad.utility.ObjectCloner;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

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

		if (adUsers.size() > municipality.getFullSyncUpperBound()) { 
			log.error("Full Sync rejected for " + municipality.getName() + " because " + adUsers.size() + " > " + municipality.getFullSyncUpperBound());
			return;
		}

		if (adUsers.size() < municipality.getFullSyncLowerBound()) { 
			log.error("Full Sync rejected for " + municipality.getName() + " because " + adUsers.size() + " < " + municipality.getFullSyncLowerBound());
			return;
		}

		try {
			if (municipality.isAzureLookupEnabled()) {
				Map<String, AzureUser> azureUsers = azureAdService.fetchAllAzureUsers(municipality);
				enrichADUsersWithAzureADLocalExtension(adUsers, azureUsers, municipality);
			}

			merge(municipality, buildADUserHashMap(municipality, adUsers), buildPersonHashMap(sofdOrganizationService.getPersons()), true);

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
				if (adUser.isDeleted()) {
					sofdOrganizationService.deleteUserByADMasterId(adUser.getObjectGuid());
				}

				// only perform synchronization if we can map user to a person by cpr
				else if (adUser.hasValidCprAttribute(municipality)) {
					if (municipality.isAzureLookupEnabled()) {
						if (!doFullFetchOfAzure) {
							Map.Entry<String, AzureUser> azureUserEntry = azureAdService.fetchAzureUserById(graphServiceClient, municipality, adUser.getUserId());
							if (azureUserEntry != null) {
								azureUsers.put(azureUserEntry.getKey(), azureUserEntry.getValue());
							}
						}

						enrichAdUser(azureUsers, municipality, adUser);
					}

					Collection<Person> persons = sofdOrganizationService.getPersons(adUser.getCpr(municipality));
					merge(municipality, buildADUserHashMap(municipality, Arrays.asList(adUser)), buildPersonHashMap(persons), false);
				}
				else {
					log.warn(adUser.getUserId() + " does not have a valid cpr '" + adUser.getCpr(municipality) + "'");
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

	private static boolean isSubstituteUser(String username) {
		final String substituteRegex = "^vik\\d+$";

		if (!StringUtils.hasLength(username)) {
			return false;
		}
		
		return username.toLowerCase().matches(substituteRegex);
	}

	private void merge(Municipality municipality, HashMap<String, List<ADUser>> adUsers, HashMap<String, Person> persons, boolean isFullSync) throws Exception {
		if (StringUtils.hasLength(municipality.getNameReplacePattern())) {
			regexReplaceChosenName(municipality, adUsers);
		}

		// find any vikXXXX users and strip displayName/chosenName as we do not want those in OS2sofd
		for (List<ADUser> users : adUsers.values()) {
			if (users != null) {
				for (ADUser user : users) {
					if (isSubstituteUser(user.getUserId())) {
						user.setChosenName(null);
					}
				}
			}
		}
		
		// find person elements that need to be created. ie. elements that are
		// not in sofd organization
		HashSet<String> toBeCreated = new HashSet<>(adUsers.keySet());
		toBeCreated.removeAll(persons.keySet());
		for (String cpr : toBeCreated) {
			if (adUsers.get(cpr).stream().anyMatch(u -> u.shouldSynchronizeUser(municipality))) {
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
		person.setCpr(firstADUser.getCpr(municipality));
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
		for (ADUser adUser : adUsers.stream().filter(u -> u.shouldSynchronizeUser(municipality)).collect(Collectors.toList())) {
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
			if (municipality.isCreateEmailEnabled()) {
				// if the AD user has an email address, we also create an Exchange user account
				if (adUser.getEmail() != null) {
					users.add(inflateExchangeUserFromAdUser(user, adUser, municipality));
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

	private User inflateExchangeUserFromAdUser(User user, ADUser adUser, Municipality municipality) throws Exception {
		User exchangeUser = new User();
		exchangeUser.setUuid(UUID.randomUUID().toString());
		exchangeUser.setMaster(adMasterIdentifier);
		exchangeUser.setMasterId(user.getUserId());
		exchangeUser.setUserId(adUser.getEmail());
		exchangeUser.setDisabled(adUser.getDisabled());
		exchangeUser.setUserType(municipality.getEmailType());

		return exchangeUser;
	}

	// we will try how this plays out with Galera/MariaDB
	private static boolean isLatin1(String v) {
//		return Charset.forName("ISO-8859-1").newEncoder().canEncode(v);
		return true;
	}

	private void handleUpdate(Municipality municipality, List<ADUser> adUsers, Person person, boolean isFullSync) throws Exception {
		Person patchedPerson = new Person();
		boolean hasActiveUsers = adUsers.stream().anyMatch(u -> u.shouldSynchronizeUser());

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

		if (!patchedPerson.equals(new Person())) {
			if (municipality.isDebugPatch()) {
				log.warn("patchedPerson before looking at user details (should only very rarely have changes): " + patchedPerson.toString());
			}
		}
		
		// Handle users
		handleUsers(municipality, adUsers, person, patchedPerson, isFullSync);

		// an update is required if the patched person differs from a new empty
		if (!patchedPerson.equals(new Person())) {
			if (municipality.isDebugPatch()) {
				log.warn("patchedPerson: " + patchedPerson.toString());
			}

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

	private void handleUsers(Municipality municipality, List<ADUser> adUsers, Person person, Person patchedPerson, boolean isFullSync) throws Exception {
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
								     u.getUserType().equals(municipality.getEmailType()) &&
								     u.getMasterId().equals(sofdUser.getUserId()))
						.findFirst();

				// note that is MITID_ERHVERV users comes from other sources (e.g. OS2faktor), then the master is different,
				// so this only updates those from AD
				User mitIdErhvervUser = null;
				if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
					Optional<User> matchingMitIDErhvervUser = sofdUsers.stream()
							.filter(u -> u.getMaster().equals(adMasterIdentifier) &&
									     u.getUserType().equals("MITID_ERHVERV") &&
									     u.getMasterId().equals("mitid-" + sofdUser.getUserId()))
							.findFirst();
	
					mitIdErhvervUser = matchingMitIDErhvervUser.isPresent() ? matchingMitIDErhvervUser.get() : null;
				}

				User emailUser = null;
				if (municipality.isCreateEmailEnabled()) {
					emailUser = matchingEmailUser.isPresent() ? matchingEmailUser.get() : null;
				}

				if (!adUser.shouldSynchronizeUser(municipality)) {
					sofdUsers.remove(sofdUser);
					
					if (emailUser != null) {
						sofdUsers.remove(emailUser);
					}

					if (mitIdErhvervUser != null) {
						sofdUsers.remove(mitIdErhvervUser);
					}

					hasUsersChanged = true;
				}
				else {
					// make a copy of user from SOFD
					User originalUser = objectCloner.deepCopy(sofdUser);

					// copy data from AD into the copy from SOFD
					inflateUserFromAdUser(municipality, sofdUser, adUser);

					// because null == 9999-12-31 we do this, so we don't get unneeded patches against SOFD (yeah I know, could be done smarter probably)
					if (originalUser.getPasswordExpireDate() == null && "9999-12-31".equals(sofdUser.getPasswordExpireDate())) {
						originalUser.setPasswordExpireDate(sofdUser.getPasswordExpireDate());
					}
					if (originalUser.getAccountExpireDate() == null && "9999-12-31".equals(sofdUser.getAccountExpireDate())) {
						originalUser.setAccountExpireDate(sofdUser.getAccountExpireDate());
					}
					
					// if employeeId is controlled from the UI, we need to ignore the value from the AD
					if (municipality.isActiveDirectoryEmployeeIdAssociationEnabled()) {
						originalUser.setEmployeeId(sofdUser.getEmployeeId());
					}
					
					// this should only ever trigger if there are ACTUAL business changes in the data
					if (!sofdUser.equals(originalUser)) {
						if (municipality.isDebugPatch()) {
							log.warn("user mismatch on patch: modifiedUser=" + sofdUser.toString() + " / originalUser=" + originalUser.toString());
						}

						hasUsersChanged = true;
					}

					// update/create derived EXCHANGE user as well (for real AD only)
					if (municipality.isCreateEmailEnabled()) {
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
								sofdUsers.add(inflateExchangeUserFromAdUser(sofdUser, adUser, municipality));

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
					
					// MitID Erhverv updates
					if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
						if (StringUtils.hasLength(adUser.getMitIDUUID())) {
							if (mitIdErhvervUser != null) {
								if (!Objects.equals(mitIdErhvervUser.getUserId(), adUser.getMitIDUUID()) ||
									!Objects.equals(mitIdErhvervUser.getDisabled(), adUser.getDisabled())) {
									mitIdErhvervUser.setUserId(adUser.getMitIDUUID());
									mitIdErhvervUser.setDisabled(adUser.getDisabled());
	
									hasUsersChanged = true;
								}
							}
							else {
								sofdUsers.add(inflateMitIDErhvervUserFromAdUser(municipality, adUser));
	
								hasUsersChanged = true;
							}
						}
						else {
							if (mitIdErhvervUser != null) {
								sofdUsers.remove(mitIdErhvervUser);
	
								hasUsersChanged = true;
							}
						}
					}
				}
			}
			else if (adUser.shouldSynchronizeUser(municipality)) {

				// special case - they might have deleted the existing AD user, and then created it again (same UserId),
				// in which case we get a new MasterID, but it is actually the same user
				Optional<User> matchingSofdUserWithNewMasterId = sofdUsers.stream()
						.filter(u -> u.getMaster().equals(adMasterIdentifier) &&
								     u.getUserId().equals(adUser.getUserId()) &&
								     u.getUserType().equals(municipality.getUserType()))
						.findFirst();

				// remove the old user with same userId from this person (but who has a new masterId)
				if (matchingSofdUserWithNewMasterId.isPresent()) {
					sofdUsers.remove(matchingSofdUserWithNewMasterId.get());
				}
				
				// no corresponding user was found. We should add one
				User user = new User();
				user.setUuid(UUID.randomUUID().toString());
				inflateUserFromAdUser(municipality, user, adUser);

				sofdUsers.add(user);

				if (municipality.isCreateEmailEnabled() && adUser.getEmail() != null) {
					sofdUsers.add(inflateExchangeUserFromAdUser(user, adUser, municipality));
				}

				if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {
					if (StringUtils.hasLength(adUser.getMitIDUUID())) {
						sofdUsers.add(inflateMitIDErhvervUserFromAdUser(municipality, adUser));
					}
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

			// filter any email users created by this master that are no longer present
			List<User> oldExchangeUsersInSofd = sofdUsers.stream()
					.filter(sofdUser -> sofdUser.getMaster().equals(adMasterIdentifier)
							&& sofdUser.getUserType().equals(municipality.getEmailType())
							&& adUsers.stream().noneMatch(adUser -> adUser.getUserId().equals(sofdUser.getMasterId())))
					.collect(Collectors.toList());

			// delete old ACTIVE_DIRECTORY accounts
			for (User oldADUserInSofd : oldADUsersInSofd) {
				sofdUsers.remove(oldADUserInSofd);
				hasUsersChanged = true;
			}

			// delete old email accounts
			for (User oldExchangeUserInSofd : oldExchangeUsersInSofd) {
				sofdUsers.remove(oldExchangeUserInSofd);
				hasUsersChanged = true;
			}

			// currently we only support reading MitID Erhverv from the ADM AD - because the user_type is global/shared,
			// and if we do not add this, then reading from school-AD will wipe any read from adm-AD (and the reverse)
			if (municipality.getUserType().equals("ACTIVE_DIRECTORY")) {

				// filter any mitIDErhverv users created by this master that are no longer present
				List<User> oldMitIDErhvervUsersInSofd = sofdUsers.stream()
						.filter(sofdUser -> sofdUser.getMaster().equals(adMasterIdentifier)
								&& sofdUser.getUserType().equals("MITID_ERHVERV")
								&& adUsers.stream().noneMatch(adUser -> ("mitid-" + adUser.getUserId()).equals(sofdUser.getMasterId())))
						.collect(Collectors.toList());
				
				// delete old mitid erhverv accounts
				for (User oldMitIDErhvervUserInSofd : oldMitIDErhvervUsersInSofd) {
					sofdUsers.remove(oldMitIDErhvervUserInSofd);
					hasUsersChanged = true;
				}
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

		for (ADUser adUser : adUsers.stream().filter(u -> u.shouldSynchronizeUser(municipality)).collect(Collectors.toList())) {
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
						&& adUsers.stream().noneMatch(adUser -> adUser.shouldSynchronizeUser(municipality) && adUser.getAffiliation() != null && adUser.getObjectGuid().equals(sofdAffiliation.getMasterId())))
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

	private User inflateMitIDErhvervUserFromAdUser(Municipality municipality, ADUser adUser) {
		User user = new User();
		user.setUuid(UUID.randomUUID().toString());
		user.setMaster(adMasterIdentifier);
		user.setMasterId("mitid-" + adUser.getUserId());
		user.setUserId(adUser.getMitIDUUID());
		user.setDisabled(adUser.getDisabled());
		user.setUserType("MITID_ERHVERV");

		return user;
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
		
		user.setWhenCreated(adUser.getWhenCreated());
		user.setEmployeeId(adUser.getEmployeeId());
		user.setUpn(StringUtils.hasLength(adUser.getUpn()) ? adUser.getUpn() : null);
		user.setTitle(StringUtils.hasLength(adUser.getTitle()) ? adUser.getTitle() : null);

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
				else if (user.getUserType().equals(municipality.getEmailType())) {
					patchUsers = true;
				}
				else if (user.getUserType().equals("MITID_ERHVERV")) {
					boolean keepIt = false;
					
					for (User adUser : person.getUsers()) {
						// only look at the right type of AD user
						if (adUser.getUserType().equalsIgnoreCase(municipality.getUserType())) {
							continue;
						}
						
						// the userId of the AD account should match the masterId of the MitID account (plus some mitid- prefix on the AD side)
						if (!("mitid-" + adUser.getUserId()).equals(user.getMasterId())) {
							continue;
						}
						
						keepIt = true;
					}
					
					if (keepIt) {
						nonADUsers.add(user);
					}
					else {
						patchUsers = true;
					}
				}
				else {
					// master is AD, but not one of the above - we keep those (even though it should never happen)
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
			phone.setMasterId("d" + adUser.getDepartmentNumber()); // department marker, for unique masterId
			phone.setPhoneNumber(adUser.getDepartmentNumber());
			phone.setPhoneType("DEPARTMENT_NUMBER");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		if (StringUtils.hasLength(adUser.getFaxNumber())) {
			Phone phone = new Phone();
			phone.setMaster(adMasterIdentifier);
			phone.setMasterId("f" + adUser.getFaxNumber()); // fax marker, for unique masterId
			phone.setPhoneNumber(adUser.getFaxNumber());
			phone.setPhoneType("FAX_NUMBER");
			phone.setVisibility(Visibility.VISIBLE);

			phones.add(phone);
		}

		return phones;
	}

	private HashMap<String, List<ADUser>> buildADUserHashMap(Municipality municipality, Collection<ADUser> adUsers) {
		HashMap<String, List<ADUser>> result = new HashMap<String, List<ADUser>>();
		for (ADUser adUser : adUsers) {
			if (!result.containsKey(adUser.getCpr(municipality))) {
				result.put(adUser.getCpr(municipality), new ArrayList<ADUser>());
			}
			
			result.get(adUser.getCpr(municipality)).add(adUser);
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
