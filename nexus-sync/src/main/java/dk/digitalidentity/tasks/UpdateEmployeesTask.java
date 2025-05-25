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
public class UpdateEmployeesTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run every night
	@Scheduled(cron = "${cron.updateEmployees:0 #{new java.util.Random().nextInt(55)} 3 * * ?}")
	public void updateEmployees() {
		log.info("Starting update employees task");

		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled()) {
				continue;
			}
            
			log.info("Updating employees for " + municipality.getName());
			nexusService.updateEmployees(municipality);
		}

		log.info("Update employees task finished");
	}
}
