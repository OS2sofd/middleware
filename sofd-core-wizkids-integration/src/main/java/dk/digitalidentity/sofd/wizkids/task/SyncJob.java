package dk.digitalidentity.sofd.wizkids.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;
import dk.digitalidentity.sofd.wizkids.service.MunicipalityService;
import dk.digitalidentity.sofd.wizkids.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class SyncJob {

	@Autowired
	private MunicipalityService municipalityService;
	
	@Autowired
	private SyncService syncService;

	@Scheduled(cron = "${cron.scheduled:0 30 4,11,17 * * MON-FRI}")
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			try {
				if (municipality.isEnabled()) {
					log.info("Synchronizing " + municipality.getName());
					
					syncService.sync(municipality);
				}
			}
			catch (Exception ex) {
				log.error(municipality.getName() + " sync failed", ex);
			}
		}
	}
}
