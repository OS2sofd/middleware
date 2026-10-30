package dk.digitalidentity.brugertjek.task;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.brugertjek.config.BrugertjekConfiguration;
import dk.digitalidentity.brugertjek.dao.auditlog.AuditLogService;
import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CleanupAuditLogTask {
	private final AuditLogService auditLogService;
	private final BrugertjekConfiguration configuration;

	// every day at 04:15:15
	@Scheduled(cron = "${cron.auditlogCleanup:15 15 4 * * ?}")
	public void cleanupAuditLog() {
		if (configuration.isScheduledEnabled()) {
			auditLogService.cleanupLogs();
		}
	}
}
