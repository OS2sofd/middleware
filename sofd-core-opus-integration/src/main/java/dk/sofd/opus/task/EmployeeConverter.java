package dk.sofd.opus.task;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dk.kmd.opus.Employee;
import dk.kmd.opus.Function;
import dk.sofd.opus.dao.model.Municipality;
import dk.sofd.opus.service.model.Affiliation;
import dk.sofd.opus.service.model.OrgUnit;
import dk.sofd.opus.service.model.Person;
import dk.sofd.opus.service.model.Post;
import dk.sofd.opus.service.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmployeeConverter {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final String addressProtectedPrefix = "*B*";

    @Value("${opus.master.identifier:OPUS}")
    private String opusMasterIdentifier;

    @Value("${opus.master.identifier:OPUS-MANAGER}")
    private String opusManagerMasterIdentifier;

	@Value("${sofd.tags.value:OPUS}")
	private String opusTagName;

    public Person toPerson(Municipality municipality, String cpr, List<Employee> employeesWithCpr, List<OrgUnit> orgUnits, List<Employee> allFilteredEmployees, Set<String> missingLosIds) throws Exception {
        // lowest work contract is usually the primary affiliation and therefore the best prototype
        // 00->Månedsløn forud,01->Månedsløn bagud,03->Måneds-/timeløn,08->Tj.pension,99->Ikke defineret
        Employee prototype = employeesWithCpr.stream().sorted(Comparator.comparing(Employee::getWorkContract)).findFirst().get();

        Person person = new Person();
        person.setUuid(UUID.randomUUID().toString());
        person.setCpr(cpr);
        person.setMaster(opusMasterIdentifier);
        person.setFirstname(prototype.getFirstName());
        person.setSurname(prototype.getLastName());

        // only add the address if the value (street) is present and doesn't contain KMD special char sequences. e.g. **ADRESSEBESKYTTET**
        if (municipality.isPrivateAddressEnabled() && prototype.getAddress() != null) {
			if (!StringUtils.isEmpty(prototype.getAddress().getValue()) && !prototype.getAddress().getValue().contains("**")) {
	            person.setRegisteredPostAddress(Post.builder()
	                    .addressProtected(prototype.getAddress().isProtected() != null ? prototype.getAddress().isProtected() : false)
	                    .city(prototype.getCity())
	                    .country(prototype.getCountry())
	                    .localname(prototype.getAddressSupplement())
	                    .postalCode(prototype.getPostalCode())
	                    .prime(true)
	                    .master(opusMasterIdentifier)
	                    .masterId(Integer.toString(prototype.getId()))
	                    .street(prototype.getAddress().getValue().replace(addressProtectedPrefix,""))
	                    .build());
	        }
        }

		if (prototype.getEntryIntoGroup() != null && !prototype.getEntryIntoGroup().isEmpty()) {
			person.setAnniversaryDate(LocalDate.parse(prototype.getEntryIntoGroup(), dateTimeFormatter));
		}

		if (prototype.getInitialEntry() != null && !prototype.getInitialEntry().isEmpty()) {
			person.setFirstEmploymentDate(LocalDate.parse(prototype.getInitialEntry(), dateTimeFormatter));
		}

		person.setAffiliations(new HashSet<>());

		for (Employee emp : employeesWithCpr) {

			// locate the uuid of the OrgUnit the affiliation points to
			String orgUnitUuid = null;
			if (municipality.isSkipOrgUnits()) {
				boolean found = false;

				for (OrgUnit orgUnit : orgUnits) {
					if (orgUnit.getTags().stream()
							.anyMatch(t -> t.getTag().equals(opusTagName) &&
									       Arrays.stream(t.getCustomValue().split(";")).anyMatch(s -> Objects.equals(s, Integer.toString(emp.getOrgUnit())))
						)) {

						// we don't want affiliations pointing to deleted orgUnits (why do they even have those?)
						if (!orgUnit.isDeleted()) {
							orgUnitUuid = orgUnit.getUuid();
						}

						found = true;
						break;
					}
				}

				if (!found) {
					missingLosIds.add(Integer.toString(emp.getOrgUnit()));
				}
			}
			else {
				for (OrgUnit orgUnit : orgUnits) {
					if (orgUnit.getMasterId().equals(Integer.toString(emp.getOrgUnit()))) {

						// we don't want affiliations pointing to deleted orgUnits (why do they even have those?)
						if (!orgUnit.isDeleted()) {
							orgUnitUuid = orgUnit.getUuid();
						}
						break;
					}
				}
			}

			if (orgUnitUuid == null) {
				log.debug("Could not find an OrgUnit with LOS-ID: " + emp.getOrgUnit());
				continue;
			}

			Date leaveDate = null, entryDate = null;
			if (emp.getEntryDate() != null && emp.getEntryDate().length() > 0) {
				entryDate = simpleDateFormatter.parse(emp.getEntryDate());
			}

			if (emp.getLeaveDate() != null && emp.getLeaveDate().length() > 0) {
				leaveDate = simpleDateFormatter.parse(emp.getLeaveDate());
			}

			if (entryDate == null) {
				// user is on "Tjenestemandspension", and is not actually employeed
				continue;
			}

			// if the municipality wants to filter out "Efterindtægt" we do so here
			if (municipality.isFilterEfterindtaegt()) {
				if (Objects.equals("efterindtægt", ((emp.getPosition() != null) ? emp.getPosition().toLowerCase() : null))) {
					continue;
				}
			}

			Affiliation affiliation = new Affiliation();
			affiliation.setUuid(UUID.randomUUID().toString());
			affiliation.setStartDate(toLocalDate(entryDate).toString());
			affiliation.setStopDate((leaveDate != null) ? (toLocalDate(leaveDate).toString()) : null);
			affiliation.setEmployeeId(Integer.toString(emp.getId()));
			affiliation.setEmploymentTerms(emp.getWorkContract());
			affiliation.setEmploymentTermsText(emp.getWorkContractText());
			setAffiliationType(affiliation, municipality);
			affiliation.setMaster(opusMasterIdentifier);
			affiliation.setMasterId(Integer.toString(emp.getId()));
			affiliation.setOrgUnitUuid(orgUnitUuid);

			// use paygrade if not null, else use paygrade text
			var payGrade = emp.getPayGrade() != null ? emp.getPayGrade() : emp.getPayGradeText();
			// ensure not null as null will cause endless updates due to patch operation in SOFD
			payGrade = payGrade != null ? payGrade : "";
			affiliation.setPayGrade(payGrade);

			if (municipality.isIncludeWageStep()) {
				affiliation.setWageStep(emp.getWageStep());
			}

			affiliation.setPositionId(emp.getPositionId() != null ? Integer.toString(emp.getPositionId()) : null);
			affiliation.setPositionName((!StringUtils.isEmpty(emp.getPosition()) ? emp.getPosition() : "Ukendt"));
			affiliation.setPositionTypeId(emp.getJobId() != null ? Integer.toString(emp.getJobId()) : null);
			affiliation.setPositionTypeName(emp.getJob());

			Double denominator = emp.getDenominator() != null ? emp.getDenominator().doubleValue() : null;
			Double numerator = emp.getNumerator() != null ? emp.getNumerator().doubleValue() : null;
			if (denominator == null || numerator == null) {
				denominator = null;
				numerator = null;
			}
			else if (denominator > 99 && numerator <= 100) {
				// special case - if the number is a percentage instead, convert to hours like this
				denominator = 37.0;
				numerator = (numerator * 37.0) / 100;
			}
			else if (denominator > 99 && numerator > 99) {
				// this is just silly - nobody works like that
				denominator = 37.0;
				numerator = 37.0;
			}

			affiliation.setWorkingHoursDenominator(denominator);
			affiliation.setWorkingHoursNumerator(numerator);
			affiliation.setFunctions(new HashSet<>());

			for (Function function : emp.getFunction()) {
				if (function.getStartDate().toGregorianCalendar().before(Calendar.getInstance()) && function.getEndDate().toGregorianCalendar().after(Calendar.getInstance())) {
					var artIdKey = String.valueOf(function.getArtId());
					if( municipality.getFunctionMap().containsKey(artIdKey)) {
						affiliation.getFunctions().add(municipality.getFunctionMap().get(artIdKey));
					}
				}
			}

			affiliation.setManagerForUuids(new HashSet<>());
			if (emp.isIsManager() != null && emp.isIsManager()) {
				// if we get SuperiorLevel from KMD, we only add this employee as manager if there is not another better choice as manager for the same orgUnit
				if( emp.getSuperiorLevel() == null || allFilteredEmployees.stream().noneMatch(e ->
						e.getOrgUnit().intValue() == emp.getOrgUnit().intValue()
						&& e.isIsManager() != null
						&& e.isIsManager()
						&& e.getId() != emp.getId()
						&& (StringUtils.isEmpty(e.getEntryDate()) || LocalDate.parse(e.getEntryDate()).isBefore(LocalDate.now().plusDays(1)))
						&& (StringUtils.isEmpty(e.getLeaveDate()) || LocalDate.parse(e.getLeaveDate()).isAfter(LocalDate.now().minusDays(1)))
						&& e.getSuperiorLevel() < emp.getSuperiorLevel()
				)) {
					affiliation.getManagerForUuids().add(orgUnitUuid);
				}
			}

			// special manager OU placement(s) for managers
			String managerOUs = null;
			if (emp.getSuperiorLevel() != null) {
				switch (emp.getSuperiorLevel()) {
					case 1:
						managerOUs = municipality.getManagerOUForLevel1();
						break;
					case 2:
						managerOUs = municipality.getManagerOUForLevel2();
						break;
					case 3:
						managerOUs = municipality.getManagerOUForLevel3();
						break;
					case 4:
						managerOUs = municipality.getManagerOUForLevel4();
						break;
					default:
						break;
				}
			}

			if (!StringUtils.isEmpty(managerOUs)) {

				for (String managerOU : managerOUs.split(",")) {
					Affiliation managerAffiliation = new Affiliation();
					managerAffiliation.setUuid(UUID.randomUUID().toString());
					managerAffiliation.setStartDate(toLocalDate(entryDate).toString());
					managerAffiliation.setStopDate((leaveDate != null) ? (toLocalDate(leaveDate).toString()) : null);
					managerAffiliation.setAffiliationType("EMPLOYEE");
					managerAffiliation.setMaster(opusManagerMasterIdentifier);
					managerAffiliation.setMasterId(Integer.toString(emp.getId()) + "-" + managerOU);
					managerAffiliation.setOrgUnitUuid(managerOU);
					managerAffiliation.setPositionId(emp.getPositionId() != null ? Integer.toString(emp.getPositionId()) : null);
					managerAffiliation.setPositionName((!StringUtils.isEmpty(emp.getPosition()) ? emp.getPosition() : "Ukendt"));
					managerAffiliation.setPositionTypeId(emp.getJobId() != null ? Integer.toString(emp.getJobId()) : null);
					managerAffiliation.setPositionTypeName(emp.getJob());
					managerAffiliation.setFunctions(new HashSet<>());
					managerAffiliation.setManagerForUuids(new HashSet<>());

					person.getAffiliations().add(managerAffiliation);
				}
			}

			Map<String, String> localExtensions = new HashMap<>();

			String[] localExtensionFields = municipality.getLocalExtensionFields() != null ? municipality.getLocalExtensionFields().split(",") : new String[]{};

			for (String extension : localExtensionFields) {
				switch (extension) {
				case "SuppId":
					if (emp.getCpr() != null && emp.getCpr().getSuppId() != null) {
						localExtensions.put("SuppId", String.valueOf(emp.getCpr().getSuppId()));
					}
					break;
				case "InvoiceRecipient":
					if (emp.isInvoiceRecipient() != null) {
						localExtensions.put("InvoiceRecipient", String.valueOf(emp.isInvoiceRecipient()));
					}
					break;
				case "InvoiceLevel1":
					if (emp.getInvoiceLevel1() != null && StringUtils.hasLength(emp.getInvoiceLevel1())) {
						localExtensions.put("InvoiceLevel1", emp.getInvoiceLevel1());
					}
					break;
				case "InvoiceLevel1Text":
					if (emp.getInvoiceLevel1Text() != null && StringUtils.hasLength(emp.getInvoiceLevel1Text())) {
						localExtensions.put("InvoiceLevel1Text", emp.getInvoiceLevel1Text());
					}
					break;
				case "InvoiceLevel2":
					if (emp.getInvoiceLevel2() != null && StringUtils.hasLength(emp.getInvoiceLevel2())) {
						localExtensions.put("InvoiceLevel2", emp.getInvoiceLevel2());
					}
					break;
				case "Level2Text":
					if (emp.getInvoiceLevel2Text() != null && StringUtils.hasLength(emp.getInvoiceLevel2Text())) {
						localExtensions.put("Level2Text", emp.getInvoiceLevel2Text());
					}
					break;
				case "SuperiorLevel":
					if (emp.getSuperiorLevel() != null) {
						localExtensions.put("SuperiorLevel", String.valueOf(emp.getSuperiorLevel()));
					}
					break;
				default:
					break;
				}
			}

			affiliation.setLocalExtensions(localExtensions);
			person.getAffiliations().add(affiliation);
		}

		// we need at least one valid affiliation to create the user
		if (person.getAffiliations().size() == 0) {
			return null;
		}

		// users
		person.setUsers(new HashSet<>());
		for (Employee emp : employeesWithCpr) {
			if (emp.getUserId() != null && emp.getUserId().length() > 0) {
				person.getUsers().add(User.builder()
						.master(opusMasterIdentifier)
						.masterId(emp.getUserId())
						.employeeId(Integer.toString(emp.getId()))
						.uuid(UUID.randomUUID().toString())
						.userId(emp.getUserId())
						.userType("OPUS")
						.build());
			}
		}

		return person;
	}

	private void setAffiliationType(Affiliation affiliation, Municipality municipality) {
		String terms = affiliation.getEmploymentTerms();

		if (municipality.getExternalEmploymentTermsList() != null && Arrays.asList(municipality.getExternalEmploymentTermsList().split(",")).contains(terms)) {
			affiliation.setAffiliationType("EXTERNAL");
		}
		else {
			affiliation.setAffiliationType("EMPLOYEE");
		}
	}

	private LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
