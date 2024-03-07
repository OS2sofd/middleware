package dk.sofd.opus.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dk.kmd.opus.Employee;
import dk.kmd.opus.Kmd;
import dk.sofd.opus.dao.model.Municipality;
import dk.sofd.opus.io.OpusXMLParser;
import dk.sofd.opus.service.model.Affiliation;
import dk.sofd.opus.service.model.OpusFilterRulesDTO;
import dk.sofd.opus.service.model.OrgUnit;
import dk.sofd.opus.service.model.Person;
import dk.sofd.opus.service.model.Phone;
import dk.sofd.opus.service.model.User;
import dk.sofd.opus.task.EmployeeConverter;
import dk.sofd.opus.task.OrgUnitConverter;
import dk.sofd.opus.task.model.KmdOrgUnitWrapper;
import dk.sofd.opus.utility.ObjectCloner;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class OpusFileParserService {

	@Value("${opus.master.identifier:OPUS}")
	private String opusMasterIdentifier;

	@Value("${opus.master.identifier:OPUS-MANAGER}")
	private String opusManagerMasterIdentifier;

	@Value("${sofd.master.identifier:SOFD}")
	private String sofdMasterIdentifier;

	@Autowired
	private OpusXMLParser opusXMLParser;

	@Autowired
	private OrgUnitConverter orgUnitConverter;

	@Autowired
	private EmployeeConverter employeeConverter;

	@Autowired
	private SofdCoreStub sofdCoreStub;

	@Autowired
	private ObjectCloner objectCloner;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private PersistentMapService persistentMap;

	@Value("${lastFilesPath}")
	private String lastFilesPath;

	@Transactional
	public void parseOpusFiles() {
		for (Municipality municipality : municipalityService.findAll()) {

			if (municipality.isDisabled()) {
				log.warn("Skipping disabled municipality: " + municipality.getName());
				continue;
			}
			
			try {
				String newestOpusFile = s3Service.getNewestFilename(municipality, "opus");

				Map<Object, Object> lastFiles = persistentMap.get(lastFilesPath);
				Object lastOpusFileName = lastFiles.get(municipality.getName() + ".opus");

				// only update if there is an opus file that has changed since last run
				if (newestOpusFile != null && !newestOpusFile.equals(lastOpusFileName)) {
					log.info("Parsing KMD supplied file for " + municipality.getName());

					InputStreamReader opusReader = s3Service.readFile(municipality, newestOpusFile);
					Kmd kmd = opusXMLParser.parseXML(opusReader);

					if (kmd.getOrgUnit().size() < 10 || kmd.getEmployee().size() < 50) {
						log.error("OPUS file for " + municipality.getName() + " failed sanity check");

						// make sure we do not attempt to parse it again
						lastFiles.put(municipality.getName() + ".opus", newestOpusFile);
						return;
					}

					OpusFilterRulesDTO filterRules = sofdCoreStub.getSettings(municipality);
					
					// make sure OrgUnits are updated
					if (!municipality.isSkipOrgUnits()) {
						log.info("Performing update on OrgUnits: " + kmd.getOrgUnit().size() + " for " + municipality.getName());
						executeOrgUnits(kmd.getOrgUnit(), municipality, filterRules);
					}

					// fetch an updated list, for updating user-references
					log.info("Re-reading all OrgUnits for " + municipality.getName());
					List<OrgUnit> orgUnits = sofdCoreStub.getOrgUnits(municipality);

					// figure out if we need extra affiliations mapped from OPUS
					Map<String, List<String>> map = sofdCoreStub.getExtraAffiliations(municipality);

					// map users and update them
					List<Employee> employees = kmd.getEmployee().stream().filter(e -> !"leave".equals(e.getAction())).collect(Collectors.toList());
					log.info("Performing update on Persons: " + employees.size() + " for " + municipality.getName());
					List<Person> updatedPerson = executePersons(employees, orgUnits, municipality, filterRules, (map.size() > 0), kmd.getOrgUnit());

					// update extra affiliations
					if (map.size() > 0) {
						updateExtraAffiliations(updatedPerson, map, municipality);
					}

					log.info("Done processing for " + municipality.getName());

					lastFiles.put(municipality.getName() + ".opus", newestOpusFile);
					persistentMap.save(lastFilesPath, lastFiles);
				}
			}
			catch (Exception e) {
				log.error("Failed to sync " + municipality.getName(), e);
			}
		}
	}

	private void updateExtraAffiliations(List<Person> persons, Map<String, List<String>> map, Municipality municipality) throws Exception {
		Map<String, Person> dirtyPersons = new HashMap<>(); // use a map to ensure we only store each person once
		Map<String, Person> personByUuid = persons.stream().collect(Collectors.toMap(Person::getUuid, Function.identity()));

		// affiliations to add
		for (String fromUuid : map.keySet()) {
			List<String> toUuids = map.get(fromUuid);

			List<Affiliation> opusAffiliations = sofdCoreStub.findOpusAffiliations(municipality, fromUuid);

			for (Affiliation opusAffiliation : opusAffiliations) {
				Person person = personByUuid.get(opusAffiliation.getPersonUuid());
				if (person == null) {
					log.warn("UpdateExtraAffiliations: Could not find person: " + opusAffiliation.getPersonUuid());
					continue;
				}

				for (String toUuid : toUuids) {
					boolean found = false;

					List<Affiliation> existingAffiliations = person.getAffiliations().stream().filter(a -> a.getMaster().equals("OPUS-SOFD")).collect(Collectors.toList());
					if (existingAffiliations != null && existingAffiliations.size() > 0) {
						for (Affiliation existingAffiliation : existingAffiliations) {
							if (existingAffiliation.getOrgUnitUuid().equals(toUuid)) {
								found = true;
								break;
							}
						}
					}

					if (!found) {
						Affiliation affiliation = new Affiliation();
						affiliation.setAffiliationType("EMPLOYEE");
						affiliation.setMaster("OPUS-SOFD");
						affiliation.setMasterId(UUID.randomUUID().toString());
						affiliation.setOrgUnitUuid(toUuid);
						affiliation.setPositionName("Medlem");
						affiliation.setStartDate(LocalDate.now().toString());
						affiliation.setUuid(UUID.randomUUID().toString());

						person.getAffiliations().add(affiliation);
						dirtyPersons.put(person.getUuid(), person);
					}
				}
			}
		}

		// affiliations to remove (physical delete)
		for (Person person : persons) {
			List<Affiliation> existingAffiliations = person.getAffiliations().stream().filter(a -> a.getMaster().equals("OPUS-SOFD")).collect(Collectors.toList());

			if (existingAffiliations != null && existingAffiliations.size() > 0) {
				for (Affiliation existingAffiliation : existingAffiliations) {
					String toUuid = existingAffiliation.getOrgUnitUuid();

					boolean found = false;

					for (String fromUuid : map.keySet()) {
						if (map.get(fromUuid).contains(toUuid)) {

							if (person.onlyActiveAffiliations().stream()
									.filter(a -> a.getMaster().equals(opusMasterIdentifier) && a.getOrgUnitUuid().equals(fromUuid))
									.count() > 0) {

								found = true;
								break;
							}
						}
					}

					// check if there is still an active OPUS affiliation allowing the hybrid affiliation, otherwise remove
					if (!found) {
						person.getAffiliations().removeIf(a -> a.getUuid().equals(existingAffiliation.getUuid()));
						dirtyPersons.put(person.getUuid(), person);
					}
				}
			}
		}

		if (dirtyPersons.size() > 0) {
			updatePersonsInSOFDCore(new ArrayList<>(dirtyPersons.values()), municipality);
		}
	}

	private void executeOrgUnits(List<dk.kmd.opus.OrgUnit> opusOrgUnits, Municipality municipality, OpusFilterRulesDTO filterRules) throws Exception {

		// create tree and then flatten it ;)
		KmdOrgUnitWrapper root = generateOrgUnitTree(opusOrgUnits, filterRules);
		List<KmdOrgUnitWrapper> flat = new ArrayList<>();
		flattenTree(root, flat);

		// read existing data from SOFD
		List<dk.sofd.opus.service.model.OrgUnit> orgUnits = sofdCoreStub.getOrgUnits(municipality);
		enrichOpusOrgUnitsWithUuids(flat, orgUnits);

		// flag all orgUnits that should be created
		flagOrgUnitsToBeCreated(flat, orgUnits);

		// flag all orgUnits that should be updated
		flagOrgUnitsToBeUpdated(municipality, flat, orgUnits);

		// find all orgUnits that should be "deleted"
		List<dk.sofd.opus.service.model.OrgUnit> orgUnitsToBeDeleted = findAllOrgUnitsNotInOpus(flat, orgUnits);

		long count = flat.stream().filter(o -> (o.isToBeCreated() || o.isToBeUpdated())).count();

		log.info("Deleting " + orgUnitsToBeDeleted.size() + ", and creating/updating " + count + " orgUnits for " + municipality.getName());

		// update SOFD Core with OrgUnit changes
		deleteOrgUnitsInSofdCore(orgUnitsToBeDeleted, municipality);
		updateCreateOrgUnitsInSofdCore(root, municipality);
	}

	private void enrichOpusOrgUnitsWithUuids(List<KmdOrgUnitWrapper> opusOrgUnits, List<dk.sofd.opus.service.model.OrgUnit> coreOrgUnits) {
		for (KmdOrgUnitWrapper opusOrgUnit : opusOrgUnits) {
			boolean found = false;

			for (dk.sofd.opus.service.model.OrgUnit coreOrgUnit : coreOrgUnits) {
				if (compares(opusOrgUnit, coreOrgUnit)) {
					opusOrgUnit.setUuid(coreOrgUnit.getUuid());
					found = true;

					break;
				}
			}

			if (!found) {
				opusOrgUnit.setUuid(UUID.randomUUID().toString());
			}
		}
	}

	private List<Person> executePersons(List<Employee> employees, List<dk.sofd.opus.service.model.OrgUnit> orgUnits, Municipality municipality, OpusFilterRulesDTO filterRules, boolean reload, List<dk.kmd.opus.OrgUnit> losUnits) throws Exception {
				
		// merge Employee records
		List<Person> opusPersons = mergeEmployeeRecords(municipality, employees, orgUnits, filterRules, losUnits);

		// read existing data from SOFD
		List<Person> persons = sofdCoreStub.getPersons(municipality);

		// find all persons that should be created
		List<Person> personsToBeCreated = findPersonsToBeCreated(opusPersons, persons);

		// find all persons that should be updated
		List<Person> personsToBeUpdated = findPersonsToBeUpdated(opusPersons, persons, filterRules);

		// find all persons that should be "deleted"
		List<Person> personsToBeDeleted = findPersonsToBeDeleted(opusPersons, persons);

		// perform SOFD updates
		log.info("Deleting " + personsToBeDeleted.size() + ", creating " + personsToBeCreated.size() + " and updating " + personsToBeUpdated.size() + " persons for " + municipality.getName());

		deletePersonsInSOFDCore(personsToBeDeleted, municipality);
		createPersonsInSOFDCore(personsToBeCreated, municipality);
		updatePersonsInSOFDCore(personsToBeUpdated, municipality);

		// fetch fresh data from SOFD, and return
		if (reload) {
			return sofdCoreStub.getPersons(municipality);
		}

		return null;
	}

	private void updatePersonsInSOFDCore(List<Person> personsToBeUpdated, Municipality municipality) throws Exception {
		for (Person personToBeUpdated : personsToBeUpdated) {
			sofdCoreStub.update(personToBeUpdated, municipality);
		}
	}

	private void createPersonsInSOFDCore(List<Person> personsToBeCreated, Municipality municipality) throws Exception {
		for (Person personToBeCreated : personsToBeCreated) {
			sofdCoreStub.create(personToBeCreated, municipality);
		}
	}

	private void deletePersonsInSOFDCore(List<Person> personsToBeDeleted, Municipality municipality) throws Exception {
		for (Person personToBeDeleted : personsToBeDeleted) {
			// yep, update not delete
			sofdCoreStub.update(personToBeDeleted, municipality);
		}
	}

	// the purpose of this method is to find any Persons in SOFD, which no longer exists in OPUS,
	// and where the SOFD person still has open OPUS data - in this case we need to set stop_dates
	// and remove OPUS accounts on that Person
	private List<Person> findPersonsToBeDeleted(List<Person> opusPersons, List<Person> persons) {
		List<Person> toBeDeleted = new ArrayList<>();
		LocalDate now = toLocalDate(new Date());

		for (Person person : persons) {
			// ignore deleted persons
			if (person.isDeleted()) {
				continue;
			}

			// can we find a matching entry in OPUS, then this is not a "delete"/cleanup scenario
			// in that case ordinary UPDATE will ensure the user gets updated correctly
			boolean found = false;
			for (Person opusPerson : opusPersons) {
				if (opusPerson.getCpr().equals(person.getCpr())) {
					found = true;
					break;
				}
			}

			if (found) {
				continue;
			}

			// does the SOFD person has any OPUS users?
			if (person.getUsers().stream().anyMatch(u -> u.getMaster().equals(opusMasterIdentifier))) {
				// there was an OPUS user on the Person, so we need to make sure the user account is removed
				toBeDeleted.add(person);

				continue;
			}

			// does the SOFD person has any open OPUS affiliations?
			for (Affiliation affiliation : person.getAffiliations()) {
				if (affiliation.getMaster().equals(opusMasterIdentifier)) {
					if (affiliation.getStopDate() == null || (affiliation.getStopDate().length() >= 10 && LocalDate.parse(affiliation.getStopDate().substring(0, 10)).isAfter(now))) {
						// person does not exist in OPUS, but SOFD think there is an open affiliation,
						// so we set a stop date to ensure the Person is correctly cleaned up (if needed) by SOFD.

						// OBS! This is not normally needed, as we get data about the end_date from OPUS,
						// and update this accordingly in good time
						toBeDeleted.add(person);
						break;
					}
				}
			}
		}

		for (Person candidate : toBeDeleted) {

			// make sure to remove all OPUS users
			candidate.getUsers().removeIf(u -> u.getMaster().equals(opusMasterIdentifier));

			// end all open affiliations (should not be needed, but this is a catch all)
			for (Affiliation affiliation : candidate.getAffiliations()) {
				if (affiliation.getMaster().equals(opusMasterIdentifier)) {
					if (affiliation.getStopDate() == null || (affiliation.getStopDate().length() >= 10 && LocalDate.parse(affiliation.getStopDate().substring(0, 10)).isAfter(now))) {
						affiliation.setStopDate(now.toString());
					}
				}
			}
		}

		return toBeDeleted;
	}

	private List<Person> findPersonsToBeUpdated(List<Person> opusPersons, List<Person> persons, OpusFilterRulesDTO rules) throws IOException {
		List<Person> toBeUpdated = new ArrayList<>();

		for (Person opusPerson : opusPersons) {
			boolean found = false;

			Person sofdPerson = null;
			for (Person person : persons) {
				if (opusPerson.getCpr().equals(person.getCpr())) {
					sofdPerson = person;
					found = true;
					break;
				}
			}

			// the user exists both in our local file and in SOFD - so we
			// "might" need to update the user
			if (found) {

				// create a copy of the sofd person
				Person originalSofdPerson = objectCloner.deepCopy(sofdPerson);

				// make changes from opus person to sofdperson
				copyFieldToSofdPerson(sofdPerson, opusPerson, rules);

				// if an existing Person has employee-typed affiliations created by SOFD, that duplicates
				// affiliations owned by OPUS, then we remove the SOFD affiliations
				removeAffiliationsOwnedBySOFDDuplicatedByOPUS(sofdPerson);

				// if they are not equal (custom equals on Person class), we
				// must make an update
				if (!sofdPerson.equals(originalSofdPerson)) {
					/* til at teste med hvis vi får "alle opdateres" under en kørsel
					if ("6cadcd98-6872-4528-94f5-0034ff54d4e4".equals(sofdPerson.getUuid())) {
						log.warn("not equals");
						log.warn("sofdPerson = " + sofdPerson.toString());
						log.warn("originalSofdPerson = " + originalSofdPerson.toString());
					}
					*/
					toBeUpdated.add(sofdPerson);
				}
			}
		}

		return toBeUpdated;
	}

	private void removeAffiliationsOwnedBySOFDDuplicatedByOPUS(Person person) {
		// unlikely, but let's check to make sure
		if (person.getAffiliations() == null) {
			return;
		}

		List<Affiliation> sofdAffiliations = person.getAffiliations().stream()
				.filter(a -> a.getMaster().equals(sofdMasterIdentifier) && a.getStopDate() == null)
				.collect(Collectors.toList());

		LocalDate tomorrow = LocalDate.now().plusDays(1);
		LocalDate yesterday = LocalDate.now().minusDays(1);

		List<Affiliation> activeOpusAffiliations = person.getAffiliations().stream()
				.filter(a -> a.getMaster().equals(opusMasterIdentifier) &&
						!a.isDeleted() &&
						(a.getStartDate() == null || (a.getStartDate().length() >= 10 && LocalDate.parse(a.getStartDate().substring(0, 10)).isBefore(tomorrow))) &&
						(a.getStopDate() == null || (a.getStopDate().length() >= 10 && LocalDate.parse(a.getStopDate().substring(0, 10)).isAfter(yesterday))))
				.collect(Collectors.toList());

		for (Affiliation sofdAffiliation : sofdAffiliations) {
			if (activeOpusAffiliations.stream().anyMatch(a -> a.getOrgUnitUuid().equals(sofdAffiliation.getOrgUnitUuid()))) {
				log.info("Stopping SOFD affiliation with masterId " + sofdAffiliation.getMasterId() + " because an active OPUS affiliation exists in the same orgunit");
				sofdAffiliation.setStopDate(toLocalDate(new Date()).toString());
			}
		}
	}

	// when performing an update, we need to use the object we got from SOFD,
	// and copy relevant fields from our OPUS object
	private void copyFieldToSofdPerson(Person sofdPerson, Person opusPerson, OpusFilterRulesDTO rules) {

		// in case AD was the original master, we "steal" it
		sofdPerson.setMaster(opusMasterIdentifier);
		sofdPerson.setAnniversaryDate(opusPerson.getAnniversaryDate());
		sofdPerson.setFirstEmploymentDate(opusPerson.getFirstEmploymentDate());

		// only do initial copy of address, or updates if noone else has taken over the address
		if (sofdPerson.getRegisteredPostAddress() == null || sofdPerson.getRegisteredPostAddress().getMaster().equals(opusMasterIdentifier)) {
			sofdPerson.setRegisteredPostAddress(opusPerson.getRegisteredPostAddress());
		}

		// make sure any existing OPUS user is removed (but keep UUID)
		Map<String, String> userUuids = new HashMap<>();
		for (Iterator<User> iterator = sofdPerson.getUsers().iterator(); iterator.hasNext(); ) {
			User user = iterator.next();

			if (user.getUserType().equals("OPUS")) {
				userUuids.put(user.getMasterId(), user.getUuid());
				iterator.remove();
			}
		}

		// then add existing ones (if we have any)
		if (opusPerson.getUsers() != null && opusPerson.getUsers().size() > 0) {
			for (User user : opusPerson.getUsers()) {

				// re-use existing UUID on OPUS user if one exists
				if (userUuids.containsKey(user.getMasterId())) {
					// existing sofd core user - set the corresponding id
					String userUuid = userUuids.get(user.getMasterId());

					user.setUuid(userUuid);
				}

				// now add to sofdPerson
				sofdPerson.getUsers().add(user);
			}
		}

		// find all affiliations owned by OPUS in SOFD Core, and perform updates on them - any new ones just get copied in AS-IS,
		// we do this to preserve the UUID on updates
		List<Affiliation> updatedAffiliations = new ArrayList<>();
		for (Iterator<Affiliation> iterator = sofdPerson.getAffiliations().iterator(); iterator.hasNext(); ) {
			Affiliation affiliation = iterator.next();

			// we only care about OPUS affiliations here
			if (!affiliation.getMaster().equals(opusMasterIdentifier) && !affiliation.getMaster().equals(opusManagerMasterIdentifier)) {
				continue;
			}

			boolean found = false;

			for (Affiliation opusAffiliation : opusPerson.getAffiliations()) {
				if (Objects.equals(opusAffiliation.getMaster(), affiliation.getMaster()) && Objects.equals(opusAffiliation.getMasterId(), affiliation.getMasterId())) {

					// preserve UUID, copy the rest
					opusAffiliation.setUuid(affiliation.getUuid());
					opusAffiliation.setPersonUuid(sofdPerson.getUuid());
					
					// if the new position name is invalid, preserve the old position name
					if (rules.isEnabled()) {
						if (rules.getInvalidPositionNames() != null && !rules.getInvalidPositionNames().isEmpty()) {
							if (rules.getInvalidPositionNames().stream().map(i -> i.toLowerCase()).collect(Collectors.toList()).contains(opusAffiliation.getPositionName().toLowerCase())) {
								opusAffiliation.setPositionName(affiliation.getPositionName());
							}
						}
					}
					
					updatedAffiliations.add(opusAffiliation);

					iterator.remove();

					found = true;
					break;
				}
			}

			// the affiliation no longer exists in the OPUS file - if no stopDate is set (or a stopDate is set in the future)
			// we update the stopDate to YESTERDAY, as that is the best we can do
			String yesterday = LocalDate.now().minusDays(1).toString();
			if (!found && (affiliation.getStopDate() == null || affiliation.getStopDate().compareTo(yesterday) > 0)) {
				affiliation.setStopDate(yesterday);
			}
		}

		// re-add the updated affiliations
		sofdPerson.getAffiliations().addAll(updatedAffiliations);

		// now find any new affiliations in the OPUS file and add to the SOFD person
		for (Affiliation affiliation : opusPerson.getAffiliations()) {
			boolean found = false;

			// does it already exist in SOFD?
			for (Affiliation sofdAffiliation : sofdPerson.getAffiliations()) {
				if (Objects.equals(sofdAffiliation.getMaster(), affiliation.getMaster()) && Objects.equals(sofdAffiliation.getMasterId(), affiliation.getMasterId())) {
					found = true;
					break;
				}
			}

			if (!found) {
				affiliation.setPersonUuid(sofdPerson.getUuid());
				sofdPerson.getAffiliations().add(affiliation);
			}
		}
	}

	private List<Person> findPersonsToBeCreated(List<Person> opusPersons, List<Person> persons) {
		List<Person> toBeCreated = new ArrayList<>();

		for (Person opusPerson : opusPersons) {
			boolean found = false;

			for (Person person : persons) {

				if (opusPerson.getCpr().equals(person.getCpr())) {
					found = true;
					break;
				}
			}

			// the user exists locally, but not in SOFD yet, so we need to
			// create the user
			if (!found) {
				toBeCreated.add(opusPerson);
			}
		}

		return toBeCreated;
	}

	private List<Person> mergeEmployeeRecords(Municipality municipality, List<Employee> employees, List<dk.sofd.opus.service.model.OrgUnit> orgUnits, OpusFilterRulesDTO filterRules, List<dk.kmd.opus.OrgUnit> losUnits) throws Exception {
		Set<String> cprs = new HashSet<>();
		List<Employee> filteredEmployees = new ArrayList<Employee>();
		Set<String> missingLosIds = new HashSet<>();
		
		for (Employee employee : employees) {
			if (filterRules.isEnabled()) {

				// filter employements based on positionIds
				if (filterRules.getPositionIds() != null && filterRules.getPositionIds().size() > 0) {
					if (filterRules.getPositionIds().contains(Integer.toString(employee.getPositionId()))) {
						continue;
					}
				}
				
				// filter employements based on positionNames
				if (filterRules.getPositionNames() != null && !filterRules.getPositionNames().isEmpty()) {
					if (filterRules.getPositionNames().stream().map(p -> p.toLowerCase()).collect(Collectors.toList()).contains(employee.getPosition().toLowerCase())) {
						continue;
					}
				}
			}

			// skip these empty records
			if (employee.getAction() != null && employee.getAction().equals("leave")) {
				continue;
			}
			filteredEmployees.add(employee);
			cprs.add(employee.getCpr().getValue());
		}

		List<Person> persons = new ArrayList<>();
		for (String cpr : cprs) {
			List<Employee> employeesWithCpr = employees.stream()
					.filter(e -> e.getCpr() != null && e.getCpr().getValue().equals(cpr))
					.collect(Collectors.toList());

			// ensure someone with a blocked position and a non-blocked position gets the blocked positions filtered here as well
			if (filterRules.isEnabled()) {
				if (filterRules.getPositionIds() != null && filterRules.getPositionIds().size() > 0) {
					employeesWithCpr = employeesWithCpr.stream()
							.filter(e -> !filterRules.getPositionIds().contains(Integer.toString(e.getPositionId())))
							.collect(Collectors.toList());
				}
			
				if (filterRules.getPositionNames() != null && !filterRules.getPositionNames().isEmpty()) {
					employeesWithCpr = employeesWithCpr.stream()
							.filter(e -> !filterRules.getPositionNames().stream().map(p -> p.toLowerCase()).collect(Collectors.toList()).contains(e.getPosition().toLowerCase()))
							.collect(Collectors.toList());
				}
			}

			Person person = employeeConverter.toPerson(municipality, cpr, employeesWithCpr, orgUnits, filteredEmployees, missingLosIds);
			if (person != null) {
				persons.add(person);
			}
		}

		Set<JSONObject> notifications = new HashSet<>();
		for (String id : missingLosIds) {
			for (dk.kmd.opus.OrgUnit losUnit : losUnits) {
				if (Objects.equals(losUnit.getId(), id)) {
					JSONObject request = new JSONObject();
					request.put("affectedEntityUuid", "");
					request.put("affectedEntityType", "ORGUNIT");
					request.put("affectedEntityName", losUnit.getLongName());
					request.put("notificationType", "UNMATCHED_WAGES_ORGUNIT");
					request.put("message", "Der er ansatte i LOS enheden " + id + " ved navn '" + losUnit.getLongName() + "', men der findes ikke noget OPUS TAG med dette ID, så de ansatte i denne enhed kommer ikke over i OS2sofd");
					request.put("eventDate", LocalDate.now().toString());

					notifications.add(request);
					break;
				}
			}
		}

		if (notifications.size() > 0) {
			log.info(municipality.getName() + ": Sending notification about " + notifications.size() + " orgunits without OPUS tag mappings!");
			sofdCoreStub.createNotifications(notifications, municipality);
		}
		
		return persons;
	}

	private void deleteOrgUnitsInSofdCore(List<dk.sofd.opus.service.model.OrgUnit> orgUnitsToBeDeleted, Municipality municipality) throws Exception {
		for (dk.sofd.opus.service.model.OrgUnit orgUnit : orgUnitsToBeDeleted) {
			sofdCoreStub.delete(orgUnit, municipality);
		}
	}

	// recursive, from root of tree, to ensure parent-relationships consistency
	private void updateCreateOrgUnitsInSofdCore(KmdOrgUnitWrapper wrapper, Municipality municipality) throws Exception {
		dk.sofd.opus.service.model.OrgUnit orgUnit = orgUnitConverter.toOrgUnit(municipality, wrapper);

		if (wrapper.isToBeCreated()) {
			sofdCoreStub.create(orgUnit, municipality);
		}
		else if (wrapper.isToBeUpdated()) {
			dk.sofd.opus.service.model.OrgUnit coreOrgUnit = wrapper.getSofdOrgUnit();

			// there might be local Phone Numbers in SOFD Core, that we need to include before performing the update
			if (coreOrgUnit != null && coreOrgUnit.getPhones() != null) {
				for (Phone phone : coreOrgUnit.getPhones()) {
					if (!phone.getMaster().equals(opusMasterIdentifier)) {
						orgUnit.getPhones().add(phone);
					}
				}
			}

			sofdCoreStub.update(orgUnit, municipality);
		}

		for (KmdOrgUnitWrapper child : wrapper.getChildren()) {
			updateCreateOrgUnitsInSofdCore(child, municipality);
		}
	}

	private List<dk.sofd.opus.service.model.OrgUnit> findAllOrgUnitsNotInOpus(List<KmdOrgUnitWrapper> opusOrgUnits, List<dk.sofd.opus.service.model.OrgUnit> coreOrgUnits) {
		List<dk.sofd.opus.service.model.OrgUnit> result = new ArrayList<>();

		for (dk.sofd.opus.service.model.OrgUnit coreOrgUnit : coreOrgUnits) {
			boolean found = false;

			if (coreOrgUnit.isDeleted()) {
				continue;
			}

			// skip OrgUnits not managed by OPUS
			if (!coreOrgUnit.getMaster().equals(opusMasterIdentifier)) {
				continue;
			}

			for (KmdOrgUnitWrapper opusOrgUnit : opusOrgUnits) {
				if (compares(opusOrgUnit, coreOrgUnit)) {
					found = true;
					break;
				}
			}

			if (!found) {
				result.add(coreOrgUnit);
			}
		}

		return result;
	}

	private void flagOrgUnitsToBeUpdated(Municipality municipality, List<KmdOrgUnitWrapper> opusOrgUnits, List<dk.sofd.opus.service.model.OrgUnit> coreOrgUnits) {
		for (KmdOrgUnitWrapper opusOrgUnit : opusOrgUnits) {
			boolean found = false;

			for (dk.sofd.opus.service.model.OrgUnit coreOrgUnit : coreOrgUnits) {
				if (compares(opusOrgUnit, coreOrgUnit)) {
					// keep a reference for later
					opusOrgUnit.setSofdOrgUnit(coreOrgUnit);

					if (changes(municipality, opusOrgUnit, coreOrgUnit)) {
						found = true;
					}

					break;
				}
			}

			if (found) {
				opusOrgUnit.setToBeUpdated(true);
			}
		}
	}

	private boolean changes(Municipality municipality, KmdOrgUnitWrapper opusOrgUnit, dk.sofd.opus.service.model.OrgUnit coreOrgUnit) {
		dk.sofd.opus.service.model.OrgUnit localVersion = orgUnitConverter.toOrgUnit(municipality, opusOrgUnit);

		// there might be local Phone Numbers in SOFD Core, that we need to include the the local version before we compare
		if (coreOrgUnit.getPhones() != null) {
			for (Phone phone : coreOrgUnit.getPhones()) {
				if (!phone.getMaster().equals(opusMasterIdentifier)) {
					localVersion.getPhones().add(phone);
				}
			}
		}

		return (!localVersion.equals(coreOrgUnit));
	}

	private void flagOrgUnitsToBeCreated(List<KmdOrgUnitWrapper> opusOrgUnits, List<dk.sofd.opus.service.model.OrgUnit> coreOrgUnits) {
		for (KmdOrgUnitWrapper opusOrgUnit : opusOrgUnits) {
			boolean found = false;

			for (dk.sofd.opus.service.model.OrgUnit coreOrgUnit : coreOrgUnits) {
				if (compares(opusOrgUnit, coreOrgUnit)) {
					found = true;
					break;
				}
			}

			if (!found) {
				opusOrgUnit.setToBeCreated(true);
			}
		}
	}

	private KmdOrgUnitWrapper generateOrgUnitTree(List<dk.kmd.opus.OrgUnit> orgUnits, OpusFilterRulesDTO filterRules) {
		for (dk.kmd.opus.OrgUnit orgUnit : orgUnits) {
			if (orgUnit.getParentOrgUnit() == null || orgUnit.getParentOrgUnit().length() == 0) {
				return getWrapperWithChildren(orgUnit, null, orgUnits, filterRules);
			}
		}

		return null;
	}

	private KmdOrgUnitWrapper getWrapperWithChildren(dk.kmd.opus.OrgUnit orgUnit, KmdOrgUnitWrapper parent, List<dk.kmd.opus.OrgUnit> orgUnits, OpusFilterRulesDTO filterRules) {
		KmdOrgUnitWrapper result = KmdOrgUnitWrapper.builder().parent(parent).children(new ArrayList<>()).orgUnit(orgUnit).build();

		for (dk.kmd.opus.OrgUnit child : orgUnits) {
			if (child.getParentOrgUnit().equals(orgUnit.getId())) {

				// exclude OrgUnits that have no valid address
				if (child.getCity() == null || child.getStreet() == null || child.getZipCode() == null) {
					log.warn("Excluding OrgUnit " + child.getLongName() + " with Id " + child.getId() + " because it has no valid address.");
					continue;
				}

				if (filterRules.isEnabled()) {

					// filter OrgUnits based on LOS-IDs in the filter-list
					if (filterRules.getLosIds() != null && filterRules.getLosIds().size() > 0) {
						if (filterRules.getLosIds().contains(child.getId())) {
							continue;
						}
					}

					// filter OrgUnits based on infix match on configured value
					if (!StringUtils.isEmpty(filterRules.getOrgUnitInfix())) {
						String[] infixes = filterRules.getOrgUnitInfix().split(";");

						boolean filtered = false;

						for (String infix : infixes) {
							if (child.getLongName().contains(infix)) {
								filtered = true;
								break;
							}
						}

						if (filtered) {
							continue;
						}
					}
				}

				KmdOrgUnitWrapper childWrapper = getWrapperWithChildren(child, result, orgUnits, filterRules);

				result.getChildren().add(childWrapper);
			}
		}

		return result;
	}

	private boolean compares(KmdOrgUnitWrapper opusOrgUnit, dk.sofd.opus.service.model.OrgUnit coreOrgUnit) {
		return (coreOrgUnit.getMaster().equals(opusMasterIdentifier) && coreOrgUnit.getMasterId().equals(opusOrgUnit.getOrgUnit().getId()));
	}

	private void flattenTree(KmdOrgUnitWrapper root, List<KmdOrgUnitWrapper> result) {
		result.add(root);

		for (KmdOrgUnitWrapper child : root.getChildren()) {
			flattenTree(child, result);
		}
	}

	private LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
