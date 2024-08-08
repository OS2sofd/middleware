package dk.digitalidentity.sofd.dao.model;

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
	private String kommunekode;
	
	@Column
	private String tabulexApiKey;
	
	@Column
	private boolean dryRun;
	
	@Column
	private long daysAfterAffiliationStops;

	@Column
	private long daysBeforeAffiliationStarts;

	@Column
	private String tagName;
	
	public String getName() {
		if (dryRun) {
			return name + " (dryrun)";
		}
		
		return name;
	}
}
