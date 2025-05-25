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
public class CreateEmployeesTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	// run every 5 minutes
	@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void createEmployees() {
		log.info("Starting create employees task");

		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled()) {
				continue;
			}
			
			log.info("Creating employees for " + municipality.getName());
			nexusService.createEmployees(municipality);
		}

		log.info("Create employees task finished");
	}
}
