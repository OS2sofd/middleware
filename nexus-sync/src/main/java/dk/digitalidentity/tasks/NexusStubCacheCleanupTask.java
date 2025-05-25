package dk.digitalidentity.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.service.nexus.NexusStub;

@Component
@EnableScheduling
public class NexusStubCacheCleanupTask {

	@Autowired
	private NexusStub nexusStub;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanUpTask() {
    	nexusStub.cleanUpOrganizationSuppliers();
    	nexusStub.cleanProfessionalJobs();
        nexusStub.cleanUpAutosignatures();
        nexusStub.cleanupFlatNexusOUs();
    }
}
