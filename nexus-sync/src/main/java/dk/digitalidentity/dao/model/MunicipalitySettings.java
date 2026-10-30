package dk.digitalidentity.dao.model;

import dk.digitalidentity.dao.model.enums.BasedOnRoleOrDefault;
import dk.digitalidentity.dao.model.enums.DataFetchType;
import dk.digitalidentity.dao.model.enums.NationalRole;
import dk.digitalidentity.dao.model.enums.UpdateType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "municipality_settings")
public class MunicipalitySettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String cvr;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType updateUpn;

	@Column
	private boolean clearCprOnLock;
	
	@Column
	private String missingVendorsMail;

	@Column
	private String createFailedEmail;

	@Column
	private boolean disableInitialsUpdate;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType organisationNameUpdateType;

	@Column
	private String nexusDefaultDepartment;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType nexusUnitUpdateType;

	// only FROM_SOFD and FROM_AD
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType nexusUnitFetchFrom;

	@Column
	private String nexusDummyEmailAddress;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType mobileUpdateType;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType workPhoneUpdateType;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType addressUpdateType;

	// only FROM_SOFD and DEFAULT_DATA
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType addressLineFetchFrom;

	@Column
	private String addressLineDefault;

	// only FROM_SOFD and DEFAULT_DATA
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType postalCodeFetchFrom;

	@Column
	private String postalCodeDefault;

	// only FROM_SOFD and DEFAULT_DATA
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType cityFetchFrom;

	@Column
	private String cityDefault;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType professionalJobUpdateType;

	// only FROM_SOFD and DEFAULT_DATA and FROM_AD
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType professionalJobFetchFrom;

	@Column
	private String professionalJobDefault;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType orgsUpdateType;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType authorisationCodeUpdateType;

	@Enumerated(EnumType.STRING)
	@Column
	private BasedOnRoleOrDefault sendToExchangeType;

	@Enumerated(EnumType.STRING)
	@Column
	private BasedOnRoleOrDefault useDefaultMedcomSenderType;

	@Enumerated(EnumType.STRING)
	@Column
	private BasedOnRoleOrDefault trustType;

	@Enumerated(EnumType.STRING)
	@Column
	private NationalRole nationalRoleDefaultValue;

	@Enumerated(EnumType.STRING)
	@Column
	private UpdateType fmkRoleUpdateType;

	// only FROM_SOFD and FROM_ROLECATALOG
	@Enumerated(EnumType.STRING)
	@Column
	private DataFetchType fmkRoleFetchFrom;

	// 0 or 1
	@Column
	private long kmdVagtplanConfiguration;
	
    @Column
    private boolean setKmdIdentity;

}
