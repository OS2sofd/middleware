package dk.digitalidentity.sofdcoreazureintegration.dao.model;

import dk.digitalidentity.sofdcoreazureintegration.security.BearerToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Municipality {
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private String sofdUrl;
	
	@Column
	private String sofdApiKey;

	@Column
	private boolean enabled;

	@Column
	private String clientId;
	
	@Column
	private String clientSecret;
	
	@Column
	private String tenantId;

	@Column
	private String cprField;
	
	@Column
	private String userIdField;

	// TODO: email?

	@Transient
	private BearerToken token;

	@Transient
	private String deltaLink;
}