package dk.digitalidentity.sofd.os2faktor.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;
import dk.digitalidentity.sofd.os2faktor.service.IntegrationService;
import dk.digitalidentity.sofd.os2faktor.service.MunicipalityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
public class SyncTask {

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private IntegrationService integrationService;

	@Scheduled(cron = "${integration.cron:30 5/15 6-20 * * ?}")
	public void fullSync() {
		for (Municipality municipality : municipalityService.findAll()) {
			try {
				integrationService.run(municipality);
			}
			catch (Exception ex) {
				log.error("Integration failed for " + municipality.getName(), ex);
			}
		}
	}
}
