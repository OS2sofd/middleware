package dk.digitalidentity.tabulex.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.sofd.config.Configuration;
import dk.digitalidentity.sofd.core.CoreService;
import dk.digitalidentity.sofd.core.model.Affiliation;
import dk.digitalidentity.sofd.core.model.OrgUnit;
import dk.digitalidentity.sofd.core.model.Person;
import dk.digitalidentity.sofd.core.model.Tag;
import dk.digitalidentity.sofd.dao.model.Municipality;
import dk.digitalidentity.sofd.service.MunicipalityService;
import dk.digitalidentity.tabulex.model.Employee;
import dk.digitalidentity.tabulex.model.ErrorResponseWrapper;
import dk.digitalidentity.tabulex.service.dto.CreateEmployeeDTO;
import dk.digitalidentity.tabulex.service.dto.DeleteEmployeeDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TabulexService {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private CoreService coreService;

	@Autowired
	private Configuration configuration;

	public void syncEmployees() {
		for (Municipality municipality : municipalityService.findAll()) {
			try {
				// read all from SOFD
				List<OrgUnit> orgUnits = coreService.getOrgUnits(municipality);
				log.info(municipality.getName() + " : SOFD OrgUnits: " + orgUnits.size());

				Predicate<OrgUnit> institutionTagPredicate = (OrgUnit ou) -> ou.getTags().stream().anyMatch(t -> t.getTag().equals(municipality.getTagName()));
				List<OrgUnit> institutions = orgUnits.stream().filter(institutionTagPredicate).collect(Collectors.toList());
				log.info(municipality.getName() + " : SOFD Institutions: " + institutions.size());
				
				List<Person> sofdPersons = coreService.getPersons(municipality);
				log.info(municipality.getName() + " : SOFD Persons: " + sofdPersons.size());

				// read existing from Tabulex
				List<Employee> tabulexEmployees = readAllEmployees(municipality);
				log.info(municipality.getName() + " : Tabulex employees: " + tabulexEmployees.size());

				// filter all SOFD and Tabulex users by relevance and group by institutionId
				Map<String, Set<String>> orgUnitUuidInstitutionMap = createOrgUnitUuidMap(municipality, institutions);
				Map<String, List<Person>> personInstitutionMap = groupPersonsByInstitution(municipality, institutions, sofdPersons);
				Map<String, List<Employee>> employeeInstitutionMap = tabulexEmployees.stream().collect(Collectors.groupingBy(Employee::getSkolekode));

				// update instititutions
				for (String institutionId : personInstitutionMap.keySet()) {
					List<Person> persons = personInstitutionMap.get(institutionId);
					List<Employee> employees = employeeInstitutionMap.get(institutionId);

					// create/update scenario
					log.info(municipality.getName() + " : Update/create on " + institutionId + " with " + (persons != null ? persons.size() : 0) + " person(s) in SOFD and " + (employees != null ? employees.size() : 0) + " employee(s) in Tabulex");

					if (persons != null) {
						for (Person person : persons) {
							boolean found = false;
	
							if (employees != null) {
								for (Employee employee : employees) {
									if (Objects.equals(employee.getCpr(), person.getCpr())) {
										found = true;
		
										updateEmployee(municipality, employee, person, institutionId, institutions, orgUnitUuidInstitutionMap.get(institutionId));
		
										break;
									}
								}
							}
							
							if (!found) {
								createEmployee(municipality, person, institutionId, institutions, orgUnitUuidInstitutionMap.get(institutionId));
							}
						}
					}
					
					// delete scenario
					log.info(municipality.getName() + " : Delete on " + institutionId);

					if (employees != null) {
						for (Employee employee : employees) {
							boolean found = false;
							
							if (persons != null) {
								for (Person person : persons) {
									if (Objects.equals(employee.getCpr(), person.getCpr())) {
										found = true;
										break;
									}							
								}
							}
							
							if (!found) {
								deleteEmployee(municipality, employee, institutionId);
							}
						}
					}
				}				
			}
			catch (Exception ex) {
				log.error(municipality.getName() + " : Sync failed", ex);
			}
		}

		log.info("Tabulex sync completed");
	}

	private Map<String, Set<String>> createOrgUnitUuidMap(Municipality municipality, List<OrgUnit> orgUnits) throws Exception {
		Map<String, Set<String>> map = new HashMap<>();

		for (OrgUnit orgUnit : orgUnits) {

			// get tag for orgUnit
			Tag institutionNumber = orgUnit.getTags()
					.stream()
					.filter(t -> t.getTag().equals(municipality.getTagName()))
					.findAny()
					.orElse(null);
	
			if (institutionNumber == null || !StringUtils.hasLength(institutionNumber.getCustomValue())) {
				throw new Exception("OrgUnit with invalid institutionNumber: " + orgUnit.getUuid());
			}
			
			String institutionNumberValue = institutionNumber.getCustomValue();
			if (!map.containsKey(institutionNumberValue)) {
				map.put(institutionNumberValue, new HashSet<>());
			}
			
			map.get(institutionNumberValue).add(orgUnit.getUuid());
		}
		
		return map;
	}

	private List<Employee> readAllEmployees(Municipality municipality) throws Exception {
		HttpEntity<HttpHeaders> request = new HttpEntity<>(getHeaders(municipality.getTabulexApiKey()));

		String url = configuration.getTabulexUrl() + "/" + municipality.getKommunekode() + "/hent";

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to fetch employees from Tabulex " + response.getStatusCode() + ", response=" + response.getBody());
		}

		ObjectMapper mapper = new ObjectMapper();

		return Arrays.asList(mapper.readValue( response.getBody(), Employee[].class));
	}

	private void updateEmployee(Municipality municipality, Employee employee, Person person, String institutionId, List<OrgUnit> orgUnits, Set<String> orgUnitUuids) throws Exception {
		boolean changes = false;

		if (!Objects.equals(employee.getAliasFornavn(), person.getFirstname())) {
			log.info(municipality.getName() + " changes detected : fornavn differs on " + person.getMaskedCpr());
			changes = true;
		}
		
		if (!Objects.equals(employee.getAliasEfternavn(), person.getSurname())) {
			log.info(municipality.getName() + " changes detected : efternavn differs on " + person.getMaskedCpr());

			changes = true;
		}
		
		ComputedAffiliation affiliation = getComputedAffiliation(municipality, person, institutionId, orgUnits, orgUnitUuids);

		if (!Objects.equals(employee.getStillingsbetegnelse(), affiliation.name())) {
			log.info(municipality.getName() + " changes detected : stillingsbetegnelse differs on " + person.getMaskedCpr());

			changes = true;
		}

		if (!Objects.equals(employee.getAfdelingNavn(), affiliation.orgUnit.orgUnitName())) {
			log.info(municipality.getName() + " changes detected : orgUnitName differs on " + person.getMaskedCpr());

			changes = true;			
		}
		
		String orgUnitId = Long.toString(affiliation.orgUnit.orgUnitId());
		if (!Objects.equals(employee.getAfdelingId(), orgUnitId)) {
			log.info(municipality.getName() + " changes detected : orgUnitId differs on " + person.getMaskedCpr());

			changes = true;
		}
		
		if (!Objects.equals(employee.getStillingsbetegnelse(), affiliation.name())) {
			log.info(municipality.getName() + " changes detected : stillingsbetegnelse differs on " + person.getMaskedCpr());

			changes = true;
		}

		if (!Objects.equals(employee.getStartDato(), affiliation.startDate())) {
			log.info(municipality.getName() + " changes detected : startDato differs on " + person.getMaskedCpr());

			changes = true;
		}

		if (!Objects.equals(employee.getSlutDato(), affiliation.stopDate())) {
			log.info(municipality.getName() + " changes detected : slutDato differs on " + person.getMaskedCpr());

			changes = true;
		}

		if (changes) {
			deleteEmployee(municipality, employee, institutionId);
			createEmployee(municipality, person, institutionId, orgUnits, orgUnitUuids);
		}
	}

	private void createEmployee(Municipality municipality, Person person, String institutionId, List<OrgUnit> orgUnits, Set<String> orgUnitUuids) throws Exception {
		log.info(municipality.getName() + " : Creating " + person.getMaskedCpr() + " in " + institutionId);
		if (municipality.isDryRun()) {
			return;
		}
		
		CreateEmployeeDTO payload = new CreateEmployeeDTO();
		payload.setAliasEfternavn(person.getSurname());
		payload.setAliasFornavn(person.getFirstname());
		payload.setCpr(person.getCpr());
		payload.setSkolekode(institutionId);
		
		ComputedAffiliation affiliation = getComputedAffiliation(municipality, person, institutionId, orgUnits, orgUnitUuids);
		payload.setStillingsbetegnelse(affiliation.name);
		payload.setStartDato(affiliation.startDate);
		payload.setSlutDato(affiliation.stopDate);
		payload.setAfdelingId(Long.toString(affiliation.orgUnit().orgUnitId));
		payload.setAfdelingNavn(affiliation.orgUnit.orgUnitName());

		HttpEntity<CreateEmployeeDTO> request = new HttpEntity<>(payload, getHeaders(municipality.getTabulexApiKey()));

		String url = configuration.getTabulexUrl() + "/" + municipality.getKommunekode() + "/opret";

		ResponseEntity<ErrorResponseWrapper> response = restTemplate.exchange(url, HttpMethod.POST, request, ErrorResponseWrapper.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to create employee in Tabulex " + response.getStatusCode() + ", response=" + response.getBody());
		}
	}

	private void deleteEmployee(Municipality municipality, Employee employee, String institutionId) throws Exception {
		log.info(municipality.getName() + " : Deleting " + employee.getMaskedCpr() + " in " + institutionId);
		if (municipality.isDryRun()) {
			return;
		}
		
		DeleteEmployeeDTO payload = new DeleteEmployeeDTO();
		payload.setCpr(employee.getCpr());
		payload.setSkolekode(institutionId);

		HttpEntity<DeleteEmployeeDTO> request = new HttpEntity<>(payload, getHeaders(municipality.getTabulexApiKey()));

		String url = configuration.getTabulexUrl() + "/" + municipality.getKommunekode() + "/slet";

		ResponseEntity<ErrorResponseWrapper> response = restTemplate.exchange(url, HttpMethod.DELETE, request, ErrorResponseWrapper.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to delete employee in Tabulex " + response.getStatusCode() + ", response=" + response.getBody());
		}
	}

	record ComputedOrgUnit(long orgUnitId, String orgUnitName) { }
	record ComputedAffiliation (String name, String startDate, String stopDate, ComputedOrgUnit orgUnit) { }
	private ComputedAffiliation getComputedAffiliation(Municipality municipality, Person person, String institutionId, List<OrgUnit> orgUnits, Set<String> orgUnitUuids) {
		List<Affiliation> affiliations = getRelevantAndActiveAffiliations(municipality, person, orgUnitUuids);
		if (affiliations.size() == 0) {
			log.warn(municipality.getName() + " : could not find any relevant affiliations for " + person.getMaskedCpr());

			return new ComputedAffiliation("Ukendt", "1970-01-01", null, new ComputedOrgUnit(0, "Ukendt"));
		}
		
		List<String> titles = new ArrayList<>();
		LocalDate startDate = LocalDate.of(1970, 1, 1);
		LocalDate stopDate = null;
		ComputedOrgUnit computedOrgUnit = new ComputedOrgUnit(0, "Ukendt");

		for (Affiliation affiliation : affiliations) {
			titles.add(affiliation.getPositionName());
			
			if (affiliation.getStopDate() != null) {
				LocalDate affiliationStopDate = LocalDate.parse(affiliation.getStopDate());

				if (stopDate != null) {
					if (stopDate.isBefore(affiliationStopDate)) {
						stopDate = affiliationStopDate;
					}
				}
				else {
					stopDate = affiliationStopDate;
				}
			}
			
			if (affiliation.getStartDate() != null) {
				LocalDate affiliationStartDate = LocalDate.parse(affiliation.getStartDate());

				if (startDate.isAfter(affiliationStartDate)) {
					startDate = affiliationStartDate;
				}
			}
			
			for (OrgUnit orgUnit : orgUnits) {
				if (Objects.equals(orgUnit.getUuid(), affiliation.getOrgUnitUuid())) {
					// can only send one - so we pick the one with the highest ID (for consistency)
					try {
						long id = Long.parseLong(orgUnit.getMasterId());

						if (id > computedOrgUnit.orgUnitId()) {
							computedOrgUnit = new ComputedOrgUnit(id, orgUnit.getName());
						}
					}
					catch (Exception ex) {
						; // ignore
					}

					break;
				}
			}
		}
		
		return new ComputedAffiliation(
			titles.stream().sorted().findFirst().orElse("Ukendt"),
			(startDate != null) ? startDate.toString() : null,
			(stopDate != null) ? stopDate.toString() : null,
			computedOrgUnit
		);
	}
	
	private List<Affiliation> getRelevantAndActiveAffiliations(Municipality municipality, Person person, Set<String> orgUnitUuids) {
		List<Affiliation> affiliations = new ArrayList<>();

		LocalDate stopDateLimit = LocalDate.now().plusDays(municipality.getDaysAfterAffiliationStops());
		LocalDate startDateLimit = LocalDate.now().minusDays(municipality.getDaysBeforeAffiliationStarts());

		for (Affiliation affiliation : person.getAffiliations()) {
			// affiliation not relevant
			if (!orgUnitUuids.contains(affiliation.getOrgUnitUuid())) {
				continue;
			}
			
			LocalDate startDate = (StringUtils.hasLength(affiliation.getStartDate())) ? LocalDate.parse(affiliation.getStartDate()) : LocalDate.of(1970, 1, 1);
			LocalDate stopDate = (StringUtils.hasLength(affiliation.getStopDate())) ? LocalDate.parse(affiliation.getStopDate()) : LocalDate.of(2099, 12, 31);
			
			// affiliation no longer active
			if (startDate.isAfter(startDateLimit) || stopDate.isBefore(stopDateLimit)) {
				continue;
			}
			
			affiliations.add(affiliation);
		}
		
		return affiliations;
	}

	private Map<String, List<Person>> groupPersonsByInstitution(Municipality municipality, List<OrgUnit> orgUnits, List<Person> persons) throws Exception {
		Map<String, List<Person>> result = new HashMap<>();

		Set<String> orgUnitUuids = orgUnits.stream().map(o -> o.getUuid()).collect(Collectors.toSet());
		Map<String, OrgUnit> orgUnitMap = orgUnits.stream().collect(Collectors.toMap(OrgUnit::getUuid, Function.identity()));
		
		for (Person person : persons) {
			for (Affiliation affiliation : getRelevantAndActiveAffiliations(municipality, person, orgUnitUuids)) {
				
				// input list of OrgUnits are valid institutions, so we can skip any affiliation not pointing to an institution
				OrgUnit orgUnit = orgUnitMap.get(affiliation.getOrgUnitUuid());
				if (orgUnit == null) {
					continue;
				}
				
				// Get tag for orgUnit
				Tag institutionNumber = orgUnit.getTags()
						.stream()
						.filter(t -> t.getTag().equals(municipality.getTagName()))
						.findAny()
						.orElse(null);

				if (institutionNumber == null || !StringUtils.hasLength(institutionNumber.getCustomValue())) {
					throw new Exception("OrgUnit with invalid institutionNumber: " + orgUnit.getUuid());
				}

				// if institution not already added to result, add institution to result
				String institutionNumberValue = institutionNumber.getCustomValue();
				if (!result.containsKey(institutionNumberValue)) {
					result.put(institutionNumberValue, new ArrayList<>());
				}

				// if person not already added to institution, add person to institution
				if (!result.get(institutionNumberValue).stream().anyMatch(p -> Objects.equals(p.getCpr(), person.getCpr()))) {
					result.get(institutionNumberValue).add(person);
				}
			}
		}

		return result;
	}

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Gravitee-Api-Key", apiKey);
		headers.add("Content-Type", "application/json;charset=UTF-8");
		headers.add("Accept", "application/json;charset=UTF-8");

		return headers;
	}
}
