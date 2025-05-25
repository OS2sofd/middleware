package dk.digitalidentity.dao.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private long nexusId;

	@Column(nullable = false)
	private String sofdPersonUuid;
	
	@Column
	private String name;

	@Column
	private String sofdKombitUuid;

	@Column
	private String ousFromAffiliations;

	@Column
	private boolean failedToCreate;
	
	@Column
	private boolean controlOrgRoles;

	@Column
	@Enumerated(EnumType.STRING)
	private FailureReason failureReason;

	@Column
	private LocalDateTime created;
	
	@Column
	private LocalDateTime lastEmployeeUpdate;
	
	@Column
	private LocalDateTime lastOrganisationUpdate;

	@Column
	private String ouName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_id")
	private Municipality municipality;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Assignment> assignments;
	
	public boolean isControlOrgRoles() {
        final String substituteRegex = "^vik\\d+$";

        // hack, to ensure we always control OrgRoles for substitutes
        if (this.userId.matches(substituteRegex)) {
        	return true;
        }

        return this.controlOrgRoles;
	}
}
