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
			var fullSync = false;

			if( syncCounter == 0) {
				// do full sync on application start for all municipalites
				fullSync = true;
			}
			else if (municipality.isDeltaSyncEnabled()) {
				// do full sync on every 30th run (every 60 minutes), but staggered by municipality id to prevent running all full syncs at the same time
				fullSync = (syncCounter + municipality.getId()) % 30 == 0;
			}
			else  {
				// do full sync on every 10th run (every 20 minutes), but staggered by municipality id to prevent running all full syncs at the same time
				fullSync = (syncCounter + municipality.getId()) % 10 == 0;
			}

			if (fullSync) {
				roleCatalogueService.performFullSync(municipality);
			}
			else if (municipality.isDeltaSyncEnabled()) {
				roleCatalogueService.performDeltaSync(municipality);
			}
		}
		
		syncCounter++;
	}
}
