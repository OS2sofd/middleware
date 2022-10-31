package dk.sofd.opus.task;

import dk.sofd.opus.service.OpusFileParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@EnableScheduling
@Component
public class OpusFileParserTask {

	@Autowired
	OpusFileParserService opusFileParserService;

	@EventListener(ApplicationReadyEvent.class)
	public void runOnStartup() {
		log.info("Running OpusFileParserTask on startup");
		opusFileParserService.parseOpusFiles();
		log.info("Sync done");
	}

	@Scheduled(cron = "${scheduler.cron}")
	public void schedueledTask() {
		log.info("Running schedueled OpusFileParserTask");
		opusFileParserService.parseOpusFiles();
		log.info("Sync done");
	}

}
