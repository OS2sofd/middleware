package dk.digitalidentity.brugertjek.dao.auditlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface AuditLogDao extends JpaRepository<AuditLog, Long> {

	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM auditlog WHERE tts < CURRENT_TIMESTAMP - INTERVAL 13 MONTH")
	void cleanupOldLogs();

}
