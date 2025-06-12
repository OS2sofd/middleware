package dk.digitalidentity.tasks;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.nexus.NexusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class ReactivateEmployeesTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run at specific intervals to ensure activation after the KMD job has disabled them
	@Scheduled(cron = "${cron.reactivateJob:0 3/15 * * * ?}")
	public void updateUsers() {
		log.info("Starting reactivation (and substitute ou fix) task");
		
		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled()) {
				continue;
			}
			
			try {
				log.info("reactivating users for " + municipality.getName());
				nexusService.reactivateEmployees(municipality);
				
				if (!municipality.isDisableOrgRoleControl()) {
					log.info("reassigning OUs for substitutes for " + municipality.getName());				
					nexusService.fixOrgRoleAssignmentsOnSubstitutes(municipality);
				}
			}
			catch (Exception ex) {
				log.error(municipality.getName() + " : updateUsers failed", ex);
			}
		}

		log.info("Reactivation (and substitute ou fix) task finished");
	}
}
