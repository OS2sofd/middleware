package dk.digitalidentity.sofd.logbuy.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import dk.digitalidentity.sofd.logbuy.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class SyncJob {
	
	@Autowired
	private SyncService syncService;

	// once per day
	@Scheduled(cron = "${cron.scheduled:0 #{new java.util.Random().nextInt(55)} 4 * * MON-FRI}")
	public void run() {
			log.info("Synchronizing to LogBuy");
			syncService.execute();
	}
}
