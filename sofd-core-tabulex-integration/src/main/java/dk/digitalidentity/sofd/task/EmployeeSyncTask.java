package dk.digitalidentity.sofd.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.tabulex.service.TabulexService;

@EnableScheduling
@Component
public class EmployeeSyncTask {

	@Autowired
	private TabulexService tabulexService;

	@Scheduled(cron = "${cron.sync:0 30 5 * * ?}")
	public void run() {
		tabulexService.syncEmployees();
	}
}
