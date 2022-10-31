package dk.sofd.core.stil.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.sofd.core.stil.dao.model.Municipality;
import dk.sofd.core.stil.service.MunicipalityService;
import dk.sofd.core.stil.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
public class SyncTask {

	@Autowired
	private SyncService syncService;

	@Scheduled(cron = "${sync.cron}")
	//@Scheduled(fixedRate = 1000000000)
	public void sync() throws Exception {
		log.info("Running STIL synchronization");
		syncService.sync();
		log.info("Done...");
	}
}
