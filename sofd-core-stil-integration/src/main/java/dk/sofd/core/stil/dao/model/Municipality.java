package dk.sofd.core.stil.dao.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

	@ElementCollection
	@CollectionTable(name = "group_map", joinColumns = @JoinColumn(name = "municipality_id"))
	@Column(name = "pattern")
	private Set<String> groupPatterns = new HashSet<>();
}