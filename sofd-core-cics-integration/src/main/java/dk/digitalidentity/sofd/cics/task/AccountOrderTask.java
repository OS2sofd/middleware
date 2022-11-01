package dk.digitalidentity.sofd.cics.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.AccountOrderService;
import dk.digitalidentity.sofd.cics.service.MunicipalityService;

@EnableScheduling
@Component
public class AccountOrderTask {

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private AccountOrderService accountOrderService;

	// every 15 minutes
	@Scheduled(fixedDelay = 15 * 60 * 1000)
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			if (municipality.isAccountOrdersEnabled()) {
				accountOrderService.execute(municipality);
			}
		}
	}
}
