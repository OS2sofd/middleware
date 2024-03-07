package dk.sofd.organization.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
	private String roleCatalogUrl;
	
	@Column
	private String roleCatalogApiKey;

	@Column
	private boolean titlesEnabled;
	
	@Column
	private boolean deltaSyncEnabled;
	
	@Column
	private boolean includeUniloginUsers;

	@Column(name = "include_school_ad_users")
	private boolean includeSchoolADUsers;

	@Column
	private String schoolDomain;

	@Column
	private boolean syncSubstitutes;
	
	@Column
	private boolean includeNonAffiliationUsers;

}