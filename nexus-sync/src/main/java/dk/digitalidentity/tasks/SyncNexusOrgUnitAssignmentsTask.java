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
public class SyncNexusOrgUnitAssignmentsTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run every 15 minutes, 3 minutes 30 seconds past (static fuzz ;))
	@Scheduled(cron = "30 3/15 * * * ?")
	public void syncNexusOrgUnits() {
		log.info("Starting sync Nexus orgUnit assignments");

		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled()) {
				continue;
			}
			
			if (municipality.isDisableOrgRoleControl()) {
				log.info("Disabled for " + municipality.getName());
				continue;
			}

			log.info("Syncing Nexus orgUnit assignments for " + municipality.getName());
			nexusService.syncNexusOrgUnitAssignments(municipality);
		}

		log.info("Sync Nexus orgUnit assignments finished");
	}
}
