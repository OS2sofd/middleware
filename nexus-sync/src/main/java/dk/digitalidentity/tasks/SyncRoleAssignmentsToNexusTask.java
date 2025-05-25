package dk.digitalidentity.tasks;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.MunicipalitySettings;
import dk.digitalidentity.dao.model.enums.BasedOnRoleOrDefault;
import dk.digitalidentity.dao.model.enums.DataFetchType;
import dk.digitalidentity.dao.model.enums.NationalRole;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.MunicipalitySettingsService;
import dk.digitalidentity.service.nexus.NexusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@EnableScheduling
@Slf4j
public class SyncRoleAssignmentsToNexusTask {

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private MunicipalitySettingsService municipalitySettingsService;

	// this task loads every single user in Nexus one by one, so lets not run so often (15:15:30 by default ;))
	@Scheduled(cron = "${cron.rolecataloguesync:30 15 15 * * ?}")
	public void syncRoleAssignmentsToNexus() {
		log.info("Starting role catalog role assignments to Nexus");

		for (Municipality municipality : municipalityService.getAll()) {
			if (municipality.isDisabled()) {
				continue;
			}
			
			if (!municipality.isRoleCatalogueRoleSyncEnabled()) {
				continue;
			}

			MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(municipality.getCvr());
			if (municipalitySettings == null) {
				log.error("No municipality settings configured, so skip " + municipality.getName());
				continue;
			}

			// check if any fetch from rc settings are used - if not skip municipality
			if (!Objects.equals(municipalitySettings.getSendToExchangeType(), BasedOnRoleOrDefault.ROLE_CATALOG) &&
				!Objects.equals(municipalitySettings.getUseDefaultMedcomSenderType(), BasedOnRoleOrDefault.ROLE_CATALOG) &&
				!Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.ROLE_CATALOG) &&
				!Objects.equals(municipalitySettings.getNationalRoleDefaultValue(), NationalRole.ROLE_CATALOG) &&
				!Objects.equals(municipalitySettings.getFmkRoleFetchFrom(), DataFetchType.FROM_ROLECATALOG)) {

				log.info("No municipality settings has fetch from RoleCatalog, so skip " + municipality.getName());
				continue;
			}

			log.info("Syncing role catalog role assignments to Nexus for " + municipality.getName());

			nexusService.syncRoleCatalogRoleAssignmentsToNexus(municipality, municipalitySettings);
		}

		log.info("Sync role catalog role assignments to Nexus finished");
	}
}
