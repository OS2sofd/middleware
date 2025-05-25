package dk.digitalidentity.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.nexus.NexusService;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class SyncNexusOrgUnitsTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run three times a day
	@Scheduled(cron = "${cron.rc.userroles.sync:0 0 5,12,18 * * *}")
	public void syncNexusOrgUnits() {
		log.info("Starting sync Nexus orgUnits to userRoles");

		for (Municipality municipality : municipalityService.getAll()) {
			// if no it-system is configured, skip synkronising to RC
			if ((municipality.isDisabled() && !municipality.isSyncOrgRolesAlways()) ||
				 municipality.getRoleCatalogueNexusItSystemId() == 0) {
				continue;
			}

			log.info("Syncing Nexus orgUnits for " + municipality.getName());
			nexusService.syncNexusOrgUnitsToUserRoles(municipality);
		}

		log.info("Sync Nexus orgUnits to userRoles finished");
	}
}
