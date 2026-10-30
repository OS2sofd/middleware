package dk.digitalidentity.brugertjek.dao.auditlog;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dk.digitalidentity.brugertjek.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {
	private final AuditLogDao auditLogDao;
	private final SecurityUtil securityUtil;

	public void logValidation(String userId) {
		AuditLog auditlog = new AuditLog();
		auditlog.setIpAddress(securityUtil.getUserIP());
		auditlog.setUserId(userId);
		auditlog.setTts(LocalDateTime.now());
		auditlog.setCvr(securityUtil.getCvr());
		auditlog.setAdmin(securityUtil.getUserId());
		
		auditLogDao.save(auditlog);
	}

	@Transactional
	public void cleanupLogs() {
		auditLogDao.cleanupOldLogs();
	}
}
