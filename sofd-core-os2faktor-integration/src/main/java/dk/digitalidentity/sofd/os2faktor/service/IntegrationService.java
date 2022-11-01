package dk.digitalidentity.sofd.os2faktor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreData;
import dk.digitalidentity.sofd.os2faktor.service.os2faktor.CoreDataEntry;
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
		List<SofdPerson> sofdPersons = sofdCoreService.getPersons(municipality);

		List<RcUser> rcUsersWithNsisAllowedRole = null;
		List<RcUser> rcUsersWithTransferToNemloginRole = null;

		if (municipality.isRoleCatalogEnabled()) {
			if (!municipality.isAllowNsisForEveryone() && municipality.getRoleCatalogRoleId() != null) {
				rcUsersWithNsisAllowedRole = roleCatalogueService.getUsersWithRoleWithRoleId(municipality, municipality.getRoleCatalogRoleId());
			}

			if (municipality.getRoleCatalogTransferToNemloginRoleId() != null) {
				rcUsersWithTransferToNemloginRole = roleCatalogueService.getUsersWithRoleWithRoleId(municipality, municipality.getRoleCatalogTransferToNemloginRoleId());
			}
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

			if (municipality.isAllowNsisForEveryone()) {
				entry.setNsisAllowed(true);
			}
			else {
				entry.setNsisAllowed(false);
				if (rcUsersWithNsisAllowedRole != null && rcUsersWithNsisAllowedRole.stream().anyMatch(rc -> Objects.equals(rc.getExtUuid(), sofdPerson.getUuid()))) {
					entry.setNsisAllowed(true);
				}
			}

			entry.setTransferToNemlogin(false);
			if (rcUsersWithTransferToNemloginRole != null && rcUsersWithTransferToNemloginRole.stream().anyMatch(rc -> Objects.equals(rc.getExtUuid(), sofdPerson.getUuid()))) {
				entry.setTransferToNemlogin(true);
			}

			if (StringUtils.hasLength(sofdPerson.getUpn())) {
				entry.getAttributes().put("upn", sofdPerson.getUpn());
			}

			coreData.getEntryList().add(entry);
		}

		// santity checks, to avoid wiping data
		if (coreData.getEntryList().size() == 0) {
			log.error("Got 0 entries for " + municipality.getName());
			return;
		}
		
		/* this does not make sense?
		if (rcUsers != null && coreData.getEntryList().stream().filter(c -> c.isNsisAllowed()).count() == 0) {
			log.error("Role Catalogue enabled, but 0 matches with SOFD Core dataset for " + municipality.getName());
			return;
		}
		*/
		
		if (!coreDataService.sendData(municipality, coreData)) {
			log.error("Submitting data to OS2faktor CoreData API failed for " + municipality.getName());
		}
		else {
			log.info("Synchronized data for " + municipality.getName());
		}
	}
}
