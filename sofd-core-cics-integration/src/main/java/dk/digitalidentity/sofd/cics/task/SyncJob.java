package dk.digitalidentity.sofd.cics.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.MunicipalityService;
import dk.digitalidentity.sofd.cics.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class SyncJob {

	@Autowired
	private MunicipalityService municipalityService;
	
	@Autowired
	private SyncService syncService;

	// twice per day
	@Scheduled(cron = "${cron.scheduled:0 #{new java.util.Random().nextInt(55)} 4,11 * * MON-FRI}")
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			log.info("Synchronizing " + municipality.getName());
			
			syncService.execute(municipality);
		}
	}
}
