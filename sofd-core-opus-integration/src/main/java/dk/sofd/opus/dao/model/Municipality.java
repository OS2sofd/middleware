package dk.sofd.opus.dao.model;

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

	// S3
	@Column
	private String bucket;

	// SOFD
	@Column
	private String apiKey;
	
	@Column
	private String url;

	// Settings
	@Column
	private short medudvalg;
	
	@Column
	private short sr;
	
	@Column
	private short tr;
	
	@Column
	private short trSuppleant;

	@Column(name = "manager_ou_for_level1")
	private String managerOUForLevel1;
	
	@Column(name = "manager_ou_for_level2")
	private String managerOUForLevel2;
	
	@Column(name = "manager_ou_for_level3")
	private String managerOUForLevel3;
	
	@Column(name = "manager_ou_for_level4")
	private String managerOUForLevel4;

	@Column(name = "filter_efter_indtaegt")
	private boolean filterEfterindtaegt;

	@Column
	private boolean includeWageStep;

	@Column(name = "use_s3_file_share")
	private boolean useS3FileShare;
	
	@Column(name = "s3_file_share_api_key")
	private String s3FileShareApiKey;

	@Column(name = "local_extensions")
	private String localExtensionFields;
}
