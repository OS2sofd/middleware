package dk.sofd.organization.dao.model;

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

}