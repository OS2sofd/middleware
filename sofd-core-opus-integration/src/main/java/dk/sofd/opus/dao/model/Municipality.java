package dk.sofd.opus.dao.model;

import java.util.HashMap;

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
	private boolean skipOrgUnits;

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

	@Column(name = "external_employment_terms_list")
	private String externalEmploymentTermsList;

	@Column
	private boolean disabled;

	@Column
	private boolean noOuPhones;

	@Column
	private String functionMap;

	public HashMap<String,String> getFunctionMap() {
		HashMap<String, String> functionHashMap = new HashMap<String, String>();
		if( this.functionMap != null ) {
			var functions = this.functionMap.split(";");
			for( var function : functions ) {
				var keyValue = function.split("=");
				if( keyValue.length == 2) {
					functionHashMap.put(keyValue[0],keyValue[1]);
				}
			}
		}
		return functionHashMap;
	}

	@Column
	private boolean privateAddressEnabled;

}
