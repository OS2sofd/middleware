package dk.digitalidentity.sofd.os2faktor.dao.model;

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
	private boolean allowNsisForEveryone;

	@Column
	private String os2faktorDomain;
	
	@Column
	private String os2faktorUrl;
	
	@Column
	private String os2faktorApiKey;

	@Column
	private String sofdUrl;

	@Column
	private String sofdApiKey;

	@Column
	private boolean roleCatalogEnabled;
	
	@Column
	private String roleCatalogUrl;
	
	@Column
	private String roleCatalogApiKey;
	
	@Column
	private String roleCatalogRoleId;

	@Column
	private String roleCatalogTransferToNemloginRoleId;

	@Column
	private int roleCatalogNegativeRoleId;

	@Column(name = "role_catalog_group_it_system_id")
	private int roleCatalogGroupITSystemId;

	@Column
	private String attributes;
	
	@Column
	private boolean writebackToSofd;

	@Column
	private boolean disabled;

	@Column
	private boolean fetchEmployeesWithoutAdOnly;
	
	@Column
	private boolean fetchAzureAdOnly;
}