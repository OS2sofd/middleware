package dk.sofd.organization.ad.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
	private String password;
	
	@Column
	private String sofdUrl;
	
	@Column
	private String sofdApiKey;
	
	@Column
	private String userType;
	
	@Column
	private String masterIdPrefix;
	
	@Column
	private boolean supportInactiveUsers;
	
	@Column
	private String nameReplacePattern;
	
	@Column
	private String nameReplaceValue;

	@Column
	private boolean azureLookupEnabled;

	@Column
	private String azureTenantId;

	@Column
	private String azureClientId;

	@Column
	private String azureSecret;

	@Column
	private String azureDomain;

	@Column
	private boolean createEmailEnabled;
	
	@Column
	private boolean encodedCpr;

	@Column
	private boolean debugPatch;
	
	private transient String clientVersion;
	private transient String tlsVersion;

	// settings, loaded on delta if never loaded, and always on full sync
	private transient boolean settingsFetched;
	private transient boolean activeDirectoryEmployeeIdAssociationEnabled;
	
	public String getEmailType() {
		if ("ACTIVE_DIRECTORY_SCHOOL".equals(userType)) {
			return "SCHOOL_EMAIL";
		}
		
		return "EXCHANGE";
	}
}