package dk.digitalidentity.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.nexus.NexusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
public class InactivateEmployeesTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run at 04:00, 08:00, 18:00
	@Scheduled(cron = "${cron.disableJob:0 0 4,8,18 * * ?}")
	public void inactivateEmployees() {
		log.info("Starting inactivate employees task");

		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled() || !municipality.isInactivationJobEnabled()) {
				continue;
			}

			log.info("Inactivating employees for " + municipality.getName());
			try {
				nexusService.inactivateEmployees(municipality);
			}
			catch (Exception ex) {
				log.error(municipality.getName() + " : Inactivation failed", ex);
			}
		}

		log.info("Inactivate employees task finished");
	}
}
