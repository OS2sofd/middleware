package dk.digitalidentity.brugertjek.task;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CleanupCache {
	private final MunicipalityService municipalityService;

	// every 10 minutes we clear cache on municipalities
	@Scheduled(cron = "0 0/10 * * * ?")
	public void clearCache() {
		municipalityService.cacheEvict();
	}
}
