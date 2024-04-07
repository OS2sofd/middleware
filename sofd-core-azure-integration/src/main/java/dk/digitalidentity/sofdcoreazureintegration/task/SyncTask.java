package dk.digitalidentity.sofdcoreazureintegration.task;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;
import dk.digitalidentity.sofdcoreazureintegration.service.AzureAdService;
import dk.digitalidentity.sofdcoreazureintegration.service.MunicipalityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
public class SyncTask {
	private long counter = 0;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private AzureAdService azureAdService;

	@Scheduled(cron = "30 1/5 5-21 * * ?")
	public void sync() throws Exception {
		// perform a full sync every 4 hours, and then delta every 5 minutes
		boolean fullSync = (counter % (4 * 12) == 0);
		counter++;

		List<Municipality> allMunicipalities = municipalityService.findAll();

		if (fullSync) {
			log.info("CoreData sync running (full)");
			
			for (Municipality municipality : allMunicipalities) {
				azureAdService.fullSync(municipality);
			}

			log.info("CoreData sync completed (full)");
			fullSync = false;
		}
		else {
			for (Municipality municipality : allMunicipalities) {
				azureAdService.deltaSync(municipality);
			}
		}
	}
}