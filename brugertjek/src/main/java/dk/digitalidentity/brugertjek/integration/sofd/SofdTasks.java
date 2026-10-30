package dk.digitalidentity.brugertjek.integration.sofd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SofdTasks {

	@Autowired
	private SofdService sofdService;
	
	// wipe cached names every day at 05:00 and 17:00
	@Scheduled(cron = "0 0 5,17 * * ?")
	public void cacheEvict() {
		sofdService.clearCache();
	}
}
