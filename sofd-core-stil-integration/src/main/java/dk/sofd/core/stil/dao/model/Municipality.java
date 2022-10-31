package dk.sofd.core.stil.dao.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

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
	private String stilUsername;
	@Column
	private String stilPassword;

	@ElementCollection
	@CollectionTable(name = "municipality_institution", joinColumns = @JoinColumn(name = "municipality_id"))
	@Column(name = "institution")
	private Set<String> stilInstitutions = new HashSet<>();

	@Column
	private String sofdUrl;
	@Column
	private String sofdApiKey;

	@Column
	private boolean enableEmail;
	@Column
	private String emailSuffix;

	@Column
	private boolean externalEnabled;

	@Column
	private boolean enabled;
}