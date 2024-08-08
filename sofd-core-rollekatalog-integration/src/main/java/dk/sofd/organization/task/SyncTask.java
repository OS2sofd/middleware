package dk.sofd.organization.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.sofd.organization.dao.model.Municipality;
import dk.sofd.organization.rc.RoleCatalogueService;
import dk.sofd.organization.service.MunicipalityService;

@EnableScheduling
@Component
public class SyncTask {
	private int syncCounter = 0;

	@Autowired
	private RoleCatalogueService roleCatalogueService;

	@Autowired
	private MunicipalityService municipalityService;

	@Scheduled(cron = "0 0/2 5-23 * * *")
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			// full sync every hour (or every 20 minutes if deltaSync is not enabled)
			if ((syncCounter % 30) == 0 || (!municipality.isDeltaSyncEnabled() && (syncCounter % 10) == 0)) {
				roleCatalogueService.performFullSync(municipality);
			}
			else if (municipality.isDeltaSyncEnabled()) {
				roleCatalogueService.performDeltaSync(municipality);
			}
		}
		
		syncCounter++;
	}
}
