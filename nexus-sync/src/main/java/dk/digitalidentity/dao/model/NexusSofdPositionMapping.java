package dk.digitalidentity.dao.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "nexus_sofd_position_mapping")
public class NexusSofdPositionMapping {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String cvr;

    @Column(nullable = false)
    private String sofdPosition;

    @Column(nullable = false)
    private String nexusProfessionalJob;

    @Column(nullable = true)
    private String nexusFmkRole;
}
