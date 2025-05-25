package dk.digitalidentity.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.MunicipalitySettings;
import dk.digitalidentity.dao.model.enums.BasedOnRoleOrDefault;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.MunicipalitySettingsService;
import dk.digitalidentity.service.nexus.NexusService;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class UpdateTrustAttestationTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private MunicipalitySettingsService municipalitySettingsService;

	// run every night
	@Scheduled(cron = "${cron.trust:0 #{new java.util.Random().nextInt(55)} 4 * * ?}")
	public void updateTrustAttestation() {
		log.info("Starting update trust attestation task");

		for (Municipality municipality : municipalityService.getAll()) {
			MunicipalitySettings settings = municipalitySettingsService.findByCvr(municipality.getCvr());
			if (settings == null) {
				continue;
			}

			if (municipality.isDisabled() || municipality.isInitialTrustSyncDone() || !settings.getTrustType().equals(BasedOnRoleOrDefault.ROLE_CATALOG)) {
				continue;
			}

			log.info("Doing initial trust attestation for " + municipality.getName());
			nexusService.updateTrustAttestation(municipality);
		}

		log.info("Initial trust attestation finished");
	}
}
