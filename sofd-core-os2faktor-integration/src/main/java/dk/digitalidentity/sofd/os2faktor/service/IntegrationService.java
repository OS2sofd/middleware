package dk.digitalidentity.sofd.os2faktor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataGroup;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataGroupLoad;
import dk.digitalidentity.sofd.os2faktor.service.rc.UserRole;
import dk.digitalidentity.sofd.os2faktor.service.rc.UserRoleAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreData;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataEntry;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataNemLoginStatus;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataService;
import dk.digitalidentity.sofd.os2faktor.service.rc.RcUser;
import dk.digitalidentity.sofd.os2faktor.service.rc.RoleCatalogueService;
import dk.digitalidentity.sofd.os2faktor.service.sofd.SofdCoreService;
import dk.digitalidentity.sofd.os2faktor.service.sofd.SofdPerson;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IntegrationService {

	@Autowired
	private RoleCatalogueService roleCatalogueService;

	@Autowired
	private CoreDataService coreDataService;
	
	@Autowired
	private SofdCoreService sofdCoreService;

	public void run(Municipality municipality) {
		ObjectMapper mapper = new ObjectMapper();

		List<SofdPerson> sofdPersons = sofdCoreService.getPersons(municipality, municipality.isFetchEmployeesWithoutAdOnly(), municipality.isFetchAzureAdOnly());

		// filter disabled and expired
		sofdPersons.removeIf(p -> p.isDisabled() || p.isExpired());

		List<RcUser> rcUsersWithNsisAllowedRole = null;
		List<RcUser> rcUsersWithTransferToNemloginRole = null;
		List<RcUser> rcUsersWithNegativeRole = null;

		try {
			if (municipality.isRoleCatalogEnabled()) {
				if (!municipality.isAllowNsisForEveryone() && municipality.getRoleCatalogRoleId() != null) {
					rcUsersWithNsisAllowedRole = roleCatalogueService.getUsersWithRoleWithRoleId(municipality, municipality.getRoleCatalogRoleId());
				}
	
				if (municipality.getRoleCatalogTransferToNemloginRoleId() != null) {
					rcUsersWithTransferToNemloginRole = roleCatalogueService.getUsersWithRoleWithRoleId(municipality, municipality.getRoleCatalogTransferToNemloginRoleId());
				}

				if (municipality.getRoleCatalogNegativeRoleId() != 0) {
					rcUsersWithNegativeRole = roleCatalogueService.getUsersWithRoleWithRoleId(municipality, Integer.toString(municipality.getRoleCatalogNegativeRoleId()));
				}
			}
		}
		catch (Exception ex) {
			log.error("Failed to fetch data from OS2rollekatalog", ex);
			return;
		}

		CoreData coreData = new CoreData();
		coreData.setDomain(municipality.getOs2faktorDomain());
		coreData.setEntryList(new ArrayList<>());
		
		for (SofdPerson sofdPerson : sofdPersons) {
			CoreDataEntry entry = new CoreDataEntry();
			entry.setCpr(sofdPerson.getCpr());
			entry.setEmail(sofdPerson.getEmail());
			entry.setName(sofdPerson.getName());
			entry.setSamAccountName(sofdPerson.getUserId());
			entry.setUuid(sofdPerson.getUuid());
			entry.setDepartment(sofdPerson.getPrimaryOrgunitName());
			entry.setNextPasswordChange(sofdPerson.getPasswordExpireDate());

			if (municipality.isAllowNsisForEveryone()) {
				entry.setNsisAllowed(true);
			}
			else {
				entry.setNsisAllowed(false);
				if (rcUsersWithNsisAllowedRole != null && rcUsersWithNsisAllowedRole.stream().anyMatch(rc -> Objects.equals(rc.getExtUuid(), sofdPerson.getUuid()))) {

					// if the user has a negative role, don't set nsisAllowed
					if (rcUsersWithNegativeRole == null || rcUsersWithNegativeRole.stream().noneMatch(u -> Objects.equals(u.getExtUuid(), sofdPerson.getUuid()))) {
						entry.setNsisAllowed(true);
					}
				}
			}

			entry.setTransferToNemlogin(false);
			if (rcUsersWithTransferToNemloginRole != null && rcUsersWithTransferToNemloginRole.stream().anyMatch(rc -> Objects.equals(rc.getExtUuid(), sofdPerson.getUuid()))) {

				// if the user has a negative role, don't set transferToNemlogin
				if (rcUsersWithNegativeRole == null || rcUsersWithNegativeRole.stream().noneMatch(u -> Objects.equals(u.getExtUuid(), sofdPerson.getUuid()))) {
					entry.setTransferToNemlogin(true);
				}
			}

			if (StringUtils.hasLength(sofdPerson.getUpn())) {
				entry.getAttributes().put("upn", sofdPerson.getUpn());
			}
			
			// this sucks a bit, but Næstved needed this information for some specific SP, and it was not registered on the AD account,
			// so we needed to pull it from SOFD for this purpose (but it overlaps the information we wish to store about department
			// for reporting, so that is okay I guess ;))
			if (StringUtils.hasLength(sofdPerson.getPrimaryOrgunitName())) {
				entry.getAttributes().put("primaryDepartment", sofdPerson.getPrimaryOrgunitName());
			}
			
			// parse localExtensions
			String attributes = municipality.getAttributes();
			if (StringUtils.hasLength(attributes) && StringUtils.hasLength(sofdPerson.getLocalExtensions())) {
				try {
					Map<String, String> map = mapper.readValue(sofdPerson.getLocalExtensions(), new TypeReference<Map<String,String>>() {});

					for (String attribute : attributes.split(",")) {
						String foundAttribute = map.get(attribute);
						if (foundAttribute != null) {
							entry.getAttributes().put(attribute, foundAttribute);
						}
					}
				}
				catch (Exception ex) {
					log.warn("Error occured while parsing localExtensions for Person: " + sofdPerson.getUuid(), ex);
				}
			}

			coreData.getEntryList().add(entry);
		}

		// sanity checks, to avoid wiping data
		if (coreData.getEntryList().size() == 0) {
			log.error("Got 0 entries for " + municipality.getName());
			return;
		}

		if (!coreDataService.sendData(municipality, coreData)) {
			log.error("Submitting data to OS2faktor CoreData API failed for " + municipality.getName());
		}
		else {
			log.info("Synchronized data for " + municipality.getName());
			
			if (municipality.isWritebackToSofd()) {
				try {
					CoreDataNemLoginStatus status = coreDataService.getNemLoginStatus(municipality);
					if (status != null) {
						sofdCoreService.loadNemLoginStatus(municipality, status);
					}
				}
				catch (Exception ex) {
					log.error("Failed to update NL3 status on " + municipality.getName(), ex);
				}
			}
		}
	}

	public void runGroupSync(Municipality municipality) {
		if (municipality.getRoleCatalogGroupITSystemId() == 0 || !municipality.isRoleCatalogEnabled()) {
			return;
		}

		log.info("Started syncing groups from OS2rollekatalog group it system for municipality " + municipality.getName());

		List<UserRole> rcGroups = new ArrayList<>();
		try {
			rcGroups = roleCatalogueService.getUserRolesFromItSystem(municipality, municipality.getRoleCatalogGroupITSystemId());
		}
		catch (Exception ex) {
			log.error("Failed to fetch group IT-system from OS2rollekatalog", ex);
			return;
		}

		CoreDataGroupLoad coreDataGroupLoad = new CoreDataGroupLoad();
		coreDataGroupLoad.setDomain(municipality.getOs2faktorDomain());
		coreDataGroupLoad.setGroups(new ArrayList<>());

		for (UserRole rcGroup : rcGroups) {
			CoreDataGroup coreDataGroup = new CoreDataGroup();
			coreDataGroup.setName(rcGroup.getRoleName());
			coreDataGroup.setDescription(rcGroup.getRoleDescription());
			coreDataGroup.setUuid(rcGroup.getRoleIdentifier());
			coreDataGroup.setMembers(new ArrayList<>(rcGroup.getAssignments().stream().map(UserRoleAssignment::getUserId).collect(Collectors.toSet())));

			coreDataGroupLoad.getGroups().add(coreDataGroup);
		}

		// sanity checks, to avoid wiping data
		if (coreDataGroupLoad.getGroups().size() == 0) {
			log.error("Got 0 group entries for " + municipality.getName());
			return;
		}

		coreDataService.syncGroups(municipality, coreDataGroupLoad);
		log.info("Finished syncing groups from OS2rollekatalog group it system for municipality " + municipality.getName());
	}
}
