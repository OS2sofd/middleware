package dk.digitalidentity.brugertjek.dao.municipality;

import org.springframework.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "municipality")
public class Municipality {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private String cvr;

	@Column
	private String sofdUrl;
	
	@Column
	private String sofdApikey;
	
	@Column
	private String rolecatalogUrl;

	@Column
	private String rolecatalogApikey;

	@Column
	private boolean nexusEnabled;

	@Column
	private String os2faktorUrl;

	@Column
	private String os2faktorApikey;

	@Column
	private String os2faktorDomain;

	@Column
	private String adApikey;

	@Column
	private String adCprfield;
	
	@Column(name = "ad_v3")
	private boolean adV3;

	@Column
	private boolean allowUserLogin;

	@Column
	private boolean disabled;

	@Column
	private String entraClientId;

	@Column
	private String entraTenantId;

	@Column
	private String entraClientSecret;

	@Transient
	public boolean isEntraEnabled() {
		return StringUtils.hasText(entraClientId) && StringUtils.hasText(entraTenantId) && StringUtils.hasText(entraClientSecret);
	}
	
	@Transient
	public boolean isAdEnabled() {
		return StringUtils.hasText(adApikey);
	}

	@Transient
	public boolean isOS2faktorEnabled() {
		return StringUtils.hasText(os2faktorUrl) && StringUtils.hasText(os2faktorApikey) && StringUtils.hasText(os2faktorDomain);
	}
	
	@Transient
	public boolean isOS2sofdEnabled() {
		return StringUtils.hasText(sofdUrl) && StringUtils.hasText(sofdApikey);
	}
	
	@Transient
	public boolean isRolecatalogEnabled() {
		return StringUtils.hasText(rolecatalogUrl) && StringUtils.hasText(rolecatalogApikey);
	}
}
