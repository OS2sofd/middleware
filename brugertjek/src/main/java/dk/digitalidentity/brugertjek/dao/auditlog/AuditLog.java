package dk.digitalidentity.brugertjek.dao.auditlog;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "auditlog")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private LocalDateTime tts;
	
	@Column
	private String cvr;

	@Column
	private String ipAddress;
	
	@Column
	private String admin;
	
	@Column
	private String userId;
}
