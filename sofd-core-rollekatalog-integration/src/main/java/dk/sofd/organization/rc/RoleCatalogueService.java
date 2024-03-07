package dk.sofd.organization.rc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.sofd.organization.core.CoreService;
import dk.sofd.organization.core.model.Affiliation;
import dk.sofd.organization.core.model.OrgUnit;
import dk.sofd.organization.core.model.Person;
import dk.sofd.organization.core.model.SyncResult;
import dk.sofd.organization.dao.model.Municipality;
import dk.sofd.organization.exception.RoleCatalogueErrorResponseException;
import dk.sofd.organization.exception.RoleCatalogueNotFoundException;
import dk.sofd.organization.rc.model.ManagerDTO;
import dk.sofd.organization.rc.model.OrgUnitDTO;
import dk.sofd.organization.rc.model.OrganisationDTO;
import dk.sofd.organization.rc.model.PositionDTO;
import dk.sofd.organization.rc.model.ResponseDTO;
import dk.sofd.organization.rc.model.TitleDTO;
import dk.sofd.organization.rc.model.UserDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoleCatalogueService {
	private Map<Long, Long> deltaSyncMap = new HashMap<>();
	
	@Autowired
	private CoreService coreService;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

    public record ManagerRecord (
        /*@Schema(description = "The name of the manager")*/ String name,
        /*@Schema(description = "The userId of the manager")*/ String userId,
        /*@Schema(description = "List of substitutes for this manager")*/ List<SubstituteRecord> substitutes
    ) { }

    public record SubstituteRecord (
    	/*@Schema(description = "The name of the substitute")*/ String name,
        /*@Schema(description = "The userId of the substitute")*/ String userId,
        /*@Schema(description = "The uuid of the orgunit the substitute is substite for")*/ String orgUnitUuid
    ) { }

	@Async
	public boolean performFullSync(Municipality municipality) {
		log.info("Performing full sync for " + municipality.getName());

		try {
			// set HEAD for deltaSyncs
			Long head = coreService.getHead(municipality);
			if (head == null) {
				throw new Exception("Failed to import. The head from SOFD should not be null.");
			}
			deltaSyncMap.put(municipality.getId(), head);

			OrgUnit[] orgUnits = coreService.getOrgUnits(municipality);
			Person[] persons = coreService.getPersons(municipality);

			// need to wrap it in ArrayList, otherwise it is immutable :(
			List<Person> personsAsList = new ArrayList<Person>(Arrays.asList(persons));
			
			// we only deal with these on full-sync, as it would be to cumbersome to do delta on them (lazy Brian ;))
			if (municipality.isIncludeNonAffiliationUsers()) {
				Person[] extraPersons = coreService.getAllAD(municipality);
				
				for (Person extraPerson : extraPersons) {
					boolean found = false;
					
					for (Person person : personsAsList) {
						if (Objects.equals(person.getUserId(), extraPerson.getUserId())) {
							found = true;
							break;
						}
					}
					
					if (!found) {
						personsAsList.add(extraPerson);
					}
				}
			}
			
			List<UserDTO> userDTOs = toDTO(personsAsList);
			List<OrgUnitDTO> orgUnitDTOs = toDTO(orgUnits, Arrays.stream(persons).toArray(Person[]::new));
			OrganisationDTO dto = new OrganisationDTO();
			dto.setOrgUnits(orgUnitDTOs);
			dto.setUsers(userDTOs);

			makeFullSyncToRoleCatalogue(dto, municipality);

			log.info("Finished full sync for " + municipality.getName());
			return true;
		}
		catch (Exception ex) {
			log.error("Sync failed (" + municipality.getName() + ")", ex);

			return false;
		}
	}

	@Async
	public boolean performDeltaSync(Municipality municipality) {
		log.info("Performing delta sync for " + municipality.getName());

		if (!deltaSyncMap.containsKey(municipality.getId())) {
			log.error("DeltaSync not possible, because no offset available for " + municipality.getName());
			return false;
		}
		
		try {
			long deltaOffset = deltaSyncMap.get(municipality.getId());
			SyncResult delta = coreService.getDeltaPersons(municipality, deltaOffset);
			if (delta == null) {
				throw new Exception("Failed to fetch delta persons from SOFD");
			}
			
			// no changes, do nothing
			if (delta.getUuids() == null || delta.getUuids().size() == 0) {
				return true;
			}
			
			// if there are more than 100 changes, just perform a full sync instead
			if (delta.getUuids().size() > 100) {
				log.warn("More than 100 changes detected, switching to full sync for " + municipality.getName());
				return performFullSync(municipality);
			}

			deltaSyncMap.put(municipality.getId(), delta.getOffset());

			Person[] persons = coreService.getPersons(municipality);

			// take only persons mentioned in the delta
			Person[] filteredPersons = Stream.of(persons)
					// TODO: remove the "Objects.equals(dp.getUuid(), p.getUuid())"-part once all municipalities are running SOFD > 2023-06-02
					.filter(p -> delta.getUuids().stream().anyMatch(dp -> Objects.equals(dp.getUuid(), p.getUuid()) || Objects.equals(dp.getUuid(), p.getPersonUuid()) ))
					.toArray(Person[]::new);

			List<UserDTO> userDTOs = toDTO(Arrays.asList(filteredPersons));

			makeDeltaSyncToRoleCatalogue(userDTOs, municipality);

			return true;
		}
		catch (Exception ex) {
			log.error("DeltaSync failed (" + municipality.getName() + ")", ex);

			return false;
		}
	}

	private List<OrgUnitDTO> toDTO(OrgUnit[] orgUnits, Person[] persons) {
		List<OrgUnitDTO> result = new ArrayList<>();

		for (OrgUnit orgUnit : orgUnits) {
			OrgUnitDTO dto = new OrgUnitDTO();
			dto.setUuid(orgUnit.getUuid());
			dto.setName(orgUnit.getName());
			dto.setInheritKle(orgUnit.isInheritKle());
			dto.setParentOrgUnitUuid(orgUnit.getParentUuid());
			dto.setKlePerforming(orgUnit.getKlePrimary());
			dto.setKleInterest(orgUnit.getKleSecondary());

			// only temporary - they are currently names, and we need to translate them to UUID's
			if (orgUnit.getTitles() != null) {
				dto.setTitleIdentifiers(orgUnit.getTitles());
			}
			else {
				dto.setTitleIdentifiers(new ArrayList<>());				
			}
			
			if (orgUnit.getManagerUuid() != null) {
				for (Person person : persons) {
					if (person.isPrime() && person.getUuid().equals(orgUnit.getManagerUuid())) {
						ManagerDTO manager = new ManagerDTO();
						manager.setUserId(person.getUserId());
						manager.setUuid(person.getUuid());

						dto.setManager(manager);
						break;
					}
				}
				
				if (dto.getManager() == null) {
					log.debug("Failed to find manager for orgUnit " + orgUnit.getUuid() + " that referenced person " + orgUnit.getManagerUuid());
				}
			}
			
			result.add(dto);
		}

		return result;
	}

	private List<UserDTO> toDTO(List<Person> persons) {
		List<UserDTO> result = new ArrayList<>();

		for (Person person : persons) {
			UserDTO dto = new UserDTO();
			dto.setSchoolUser(person.isSchoolUser());
			dto.setExtUuid(person.getUuid());
			dto.setName(person.getName());
			dto.setCpr(person.getCpr());
			dto.setNemloginUuid(person.getNemloginUserUuid());
			dto.setEmail(person.getEmail());
			dto.setPhone(person.getPhone());
			dto.setUserId(person.getUserId());
			dto.setDoNotInherit(person.isDoNotInherit());
			dto.setDisabled(person.isDisabled());
			dto.setPositions(new ArrayList<>());

			if (person.getAffiliations() != null) {
				for (Affiliation affiliation : person.getAffiliations()) {
					PositionDTO position = new PositionDTO();
					position.setName(affiliation.getPositionName());
					position.setOrgUnitUuid(affiliation.getOrgUnitUuid());
					// null-check that supports SOFD versions still not sending do not inherit on affiliations
					// change once all SOFD instances have version >= 2022-12-05
					position.setDoNotInherit(affiliation.getDoNotInherit() != null ? affiliation.getDoNotInherit() : person.isDoNotInherit());
					dto.getPositions().add(position);
				}
			}
			
			dto.setKlePerforming(person.getKlePrimary());
			dto.setKleInterest(person.getKleSecondary());
			
			result.add(dto);
		}

		return result;
	}

	private void makeFullSyncToRoleCatalogue(OrganisationDTO dto, Municipality municipality) throws Exception {
		// load titles first (if enabled)
		if (municipality.isTitlesEnabled()) {
			// load existing titles (so we let the role catalogue be our UUID holder
			
			HttpEntity<?> titleGetRequest = new HttpEntity<>(getHeaders(municipality.getRoleCatalogApiKey()));
			ResponseEntity<TitleDTO[]> titleGetResponse = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/title", HttpMethod.GET, titleGetRequest, TitleDTO[].class);
			if (!titleGetResponse.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to load Titles from RoleCatalogue (" + municipality.getName() + "). " + titleGetResponse.getStatusCodeValue() + ", response=" + titleGetResponse.getBody());
			}

			TitleDTO[] aTitles = titleGetResponse.getBody();
			List<TitleDTO> titles = Arrays.asList(aTitles);
			Map<String, TitleDTO> mapOfTitles = titles.stream().collect(Collectors.toMap(TitleDTO::getName, t -> t));

		    // keep track of any changes to the Titles payload
			boolean changes = false;
			
			// update User objects and track changes to the mapOfTitles
			for (UserDTO userDTO : dto.getUsers()) {
				if (userDTO.getPositions() != null) {
					for (PositionDTO positionDTO : userDTO.getPositions()) {
						TitleDTO title = mapOfTitles.get(positionDTO.getName());
						if (title == null) {
							title = new TitleDTO();
							title.setUuid(UUID.randomUUID().toString());
							title.setName(positionDTO.getName());
							
							mapOfTitles.put(title.getName(), title);

							changes = true;
						}
						
						positionDTO.setTitleUuid(title.getUuid());
					}
				}
			}
			
			// update OrgUnit objets and track changes to the mapOfTitles
			for (OrgUnitDTO orgUnitDTO : dto.getOrgUnits()) {
				if (orgUnitDTO.getTitleIdentifiers() != null && orgUnitDTO.getTitleIdentifiers().size() > 0) {
					List<String> titleIdentifiers = new ArrayList<>();

					// translate them all into actual UUID identifiers
					for (String titleName : orgUnitDTO.getTitleIdentifiers()) {
						TitleDTO title = mapOfTitles.get(titleName);
						if (title == null) {
							title = new TitleDTO();
							title.setUuid(UUID.randomUUID().toString());
							title.setName(titleName);

							mapOfTitles.put(title.getName(), title);

							changes = true;
						}
						
						titleIdentifiers.add(title.getUuid());
					}

					orgUnitDTO.setTitleIdentifiers(titleIdentifiers);
				}
			}

			// finally load back if there are changes
			if (changes) {
				aTitles = mapOfTitles.values().toArray(new TitleDTO[0]);

				HttpEntity<TitleDTO[]> titleSaveRequest = new HttpEntity<>(aTitles, getHeaders(municipality.getRoleCatalogApiKey()));

				ResponseEntity<String> titleSaveResponse = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/title", HttpMethod.POST, titleSaveRequest, String.class);
				if (!titleSaveResponse.getStatusCode().equals(HttpStatus.OK)) {
					throw new Exception("Failed to import Titles to RoleCatalogue (" + municipality.getName() + "). " + titleSaveResponse.getStatusCodeValue() + ", response=" + titleSaveResponse.getBody());
				}
			}
		}
		
		// then load organisation
		// First update primary domain (non-school)
		var nonSchoolUsers =  dto.getUsers().stream().filter(u -> !u.isSchoolUser()).toList();
		var schoolUsers = dto.getUsers().stream().filter(u -> u.isSchoolUser()).toList();

		// first do the update to primary domain (non-school-domain)
		dto.setUsers(nonSchoolUsers);
		HttpEntity<OrganisationDTO> request = new HttpEntity<>(dto, getHeaders(municipality.getRoleCatalogApiKey()));
		ResponseEntity<ResponseDTO> response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/organisation/v3", HttpMethod.POST, request, ResponseDTO.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to import to RoleCatalogue primary domain (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
		ResponseDTO responseDTO = response.getBody();
		if (responseDTO.containsChanges()) {
			log.info("RoleCatalogue primary domain updated (" + municipality.getName() + "): " + responseDTO.toString());
		}

		if( municipality.isIncludeSchoolADUsers() )
		{
			// then do the update to school domain
			dto.setUsers(schoolUsers);
			log.trace("School users full sync payload: " + objectMapper.writeValueAsString(dto));
			request = new HttpEntity<>(dto, getHeaders(municipality.getRoleCatalogApiKey()));
			response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/organisation/v3?domain=" + municipality.getSchoolDomain(), HttpMethod.POST, request, ResponseDTO.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to import to RoleCatalogue school domain (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}
			responseDTO = response.getBody();
			if (responseDTO.containsChanges()) {
				log.info("RoleCatalogue school domain updated (" + municipality.getName() + "): " + responseDTO.toString());
			}
		}

	}

	private void makeDeltaSyncToRoleCatalogue(List<UserDTO> users, Municipality municipality) throws Exception {
		// load titles first (if enabled)
		if (municipality.isTitlesEnabled()) {
			// load existing titles (so we let the role catalogue be our UUID holder
			
			HttpEntity<?> titleGetRequest = new HttpEntity<>(getHeaders(municipality.getRoleCatalogApiKey()));
			ResponseEntity<TitleDTO[]> titleGetResponse = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/title", HttpMethod.GET, titleGetRequest, TitleDTO[].class);
			if (!titleGetResponse.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to load Titles from RoleCatalogue (" + municipality.getName() + "). " + titleGetResponse.getStatusCodeValue() + ", response=" + titleGetResponse.getBody());
			}

			TitleDTO[] aTitles = titleGetResponse.getBody();
			List<TitleDTO> titles = Arrays.asList(aTitles);
			Map<String, TitleDTO> mapOfTitles = titles.stream().collect(Collectors.toMap(TitleDTO::getName, t -> t));

			for (UserDTO userDTO : users) {
				if (userDTO.getPositions() != null) {
					for (PositionDTO positionDTO : userDTO.getPositions()) {
						TitleDTO title = mapOfTitles.get(positionDTO.getName());
						if (title != null) {
							positionDTO.setTitleUuid(title.getUuid());
						}
					}
				}
			}
		}
		
		// then load users to RoleCatalog
		// First update primary domain (non-school)
		HttpEntity<List<UserDTO>> request = new HttpEntity<>(users.stream().filter(u -> !u.isSchoolUser()).toList(), getHeaders(municipality.getRoleCatalogApiKey()));

		ResponseEntity<ResponseDTO> response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/organisation/v3/delta", HttpMethod.POST, request, ResponseDTO.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to delta import to RoleCatalogue primary domain (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		ResponseDTO responseDTO = response.getBody();
		if (responseDTO.containsChanges()) {
			log.info("RoleCatalogue primary domain delta updated (" + municipality.getName() + "): " + responseDTO.toString());
		}

		// then do the update to school domain
		if( municipality.isIncludeSchoolADUsers() ) {
			request = new HttpEntity<>(users.stream().filter(u -> u.isSchoolUser()).toList(), getHeaders(municipality.getRoleCatalogApiKey()));

			response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/organisation/v3/delta?domain=" + municipality.getSchoolDomain(), HttpMethod.POST, request, ResponseDTO.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				throw new Exception("Failed to delta import to RoleCatalogue primary domain (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", response=" + response.getBody());
			}

			responseDTO = response.getBody();
			if (responseDTO.containsChanges()) {
				log.info("RoleCatalogue primary domain delta updated (" + municipality.getName() + "): " + responseDTO.toString());
			}
		}

	}
	
	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "application/json");

		return headers;
	}

	public List<ManagerRecord> getAllSubstitutes(Municipality municipality) throws Exception {
		HttpEntity<?> managerSubstituteGetRequest = new HttpEntity<>(getHeaders(municipality.getRoleCatalogApiKey()));

		ResponseEntity<String> testResponse = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/manager", HttpMethod.GET, managerSubstituteGetRequest, String.class);
		if (!testResponse.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to load managerSubstitutes from RoleCatalogue (" + municipality.getName() + "). " + testResponse.getStatusCodeValue() + ", response=" + testResponse.getBody());
		}

		if (log.isDebugEnabled()) {
			log.debug("Response:\r\n" + testResponse.getBody());
			log.debug(municipality.getRoleCatalogUrl() + "/manager");
		}

		ResponseEntity<ManagerRecord[]> managerSubstituteGetResponse = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/manager", HttpMethod.GET, managerSubstituteGetRequest, ManagerRecord[].class);
		if (!managerSubstituteGetResponse.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to load managerSubstitutes from RoleCatalogue (" + municipality.getName() + "). " + managerSubstituteGetResponse.getStatusCodeValue() + ", response=" + managerSubstituteGetResponse.getBody());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Response:\r\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(managerSubstituteGetResponse.getBody()));
		}
		
		return Arrays.asList(managerSubstituteGetResponse.getBody());
	}
	
	public void createSubstitute(Municipality municipality, ManagerRecord body) throws Exception {
		HttpEntity<ManagerRecord> request = new HttpEntity<>(body, getHeaders(municipality.getRoleCatalogApiKey()));

		if (log.isDebugEnabled()) {
			log.debug("Request:\r\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
		}
		
		ResponseEntity<?> response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/manager", HttpMethod.POST, request, String.class);

		if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
			String payload = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request);
			
			throw new RoleCatalogueNotFoundException("Got not found when trying to create substitute (" + municipality.getName() + "), payload=" + payload + ", response=" + response.getBody());
		}
		else if (response.getStatusCode().isError()) {
			String payload = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request);
			
			throw new RoleCatalogueErrorResponseException("Failed to create substitute (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", payload=" + payload + ", response=" + response.getBody());
		}
	}
	
	public void deleteSubstitute(Municipality municipality, ManagerRecord body) throws Exception {
		HttpEntity<ManagerRecord> request = new HttpEntity<>(body, getHeaders(municipality.getRoleCatalogApiKey()));

		if (log.isDebugEnabled()) {
			log.debug("Request:\r\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
		}
		
		ResponseEntity<?> response = restTemplate.exchange(municipality.getRoleCatalogUrl() + "/manager", HttpMethod.DELETE, request, String.class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new Exception("Failed to delete substitute (" + municipality.getName() + "). " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}
	}
}
