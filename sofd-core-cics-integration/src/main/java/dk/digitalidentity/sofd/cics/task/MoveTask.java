package dk.digitalidentity.sofd.cics.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.MoveService;
import dk.digitalidentity.sofd.cics.service.MunicipalityService;

@EnableScheduling
@Component
public class MoveTask {

	@Autowired
	private MunicipalityService municipalityService;
	
	@Autowired
	private MoveService moveService;

	// nightly
	@Scheduled(cron = "${cron.scheduled:0 #{new java.util.Random().nextInt(55)} 3 * * MON-FRI}")
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			if (municipality.isAccountOrdersEnabled()) {
				moveService.execute(municipality);
			}
		}
	}
}
