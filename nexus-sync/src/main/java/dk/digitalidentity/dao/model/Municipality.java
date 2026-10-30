package dk.digitalidentity.dao.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "municipality")
public class Municipality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column
    private boolean disabled;

    @Column(nullable = false)
    private String cvr;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String roleCatalogueBaseUrl;

    @Column(nullable = false)
    private String roleCatalogueApiKey;

    @Column(nullable = false)
    private int roleCatalogueNexusItSystemId;

    @Column(nullable = false)
    private String nexusBaseUrl;

    @Column(nullable = false)
    private String nexusTokenUrl;

    @Column(nullable = false)
    private String nexusClientId;

    @Column(nullable = false)
    private String nexusClientSecret;

    @Column(name = "role_catalogue_kmd_nexus_it_system_id", nullable = false)
    private String roleCatalogueKMDNexusItSystemId;

    @Column
    private String sofdBaseUrl;

    @Column
    private String sofdApiKey;

    @Column
    private long sofdSyncHead;
    
    @Column
    private boolean disableOrgRoleControl;
    
    @Enumerated(EnumType.STRING)
    @Column
    private InitialsChoice initialsChoice;

    @Column
    private boolean roleCatalogueRoleSyncEnabled;
    
    // These two values are used once, and ensure we can migrate Trust roles into OS2rollekatalog
    
    @Column
    private boolean initialTrustSyncDone;
    
    @Column
    private String roleCatalogueTrustRoleId;
    
    // we need to wait for RKSK before we can remove this setting, as they still run the old model
    @Column
    private boolean syncAllUsers;
        
    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @Column
    private boolean syncOrgRolesAlways;

    @Column
    private long defaultOu;

    @Column
    private boolean inactivationJobEnabled;

}
