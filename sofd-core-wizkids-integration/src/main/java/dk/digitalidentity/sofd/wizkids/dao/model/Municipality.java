package dk.digitalidentity.sofd.wizkids.dao.model;

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
	private boolean enabled;
	
	@Column
	private String bearerToken;
	
	@Column
	private String mailDomain;
	
	@Column
	private String sofdUrl;
	
	@Column
	private String sofdApiKey;

}