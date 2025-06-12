package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//TODO: on purpose, we want to fail when they add new fields
@JsonIgnoreProperties(ignoreUnknown = false)
public class EmployeeConfiguration {
    private int id;
    private int version;
    private String stsSn;
    private boolean active = true;
    
    // Dette er Systemindstillinger skærmbilledet fra Nexus
 
    // sættes ved oprettelse, og opdateres dagligt
    private String primaryIdentifier;                                        // Unik ID              -  sættes pt til brugernavnet på den primære AD konto
    private DefaultOrganizationSupplier defaultOrganizationSupplier;         // Standardleverandør på arbejdstider  - afhænger af match med primaryOrganization
    private AuthorizationCodeConfiguration authorizationCodeConfiguration;   // Autorisationskode    -  sættes ved oprettelse, og ved opdateringer kun hvis den ikke er sat i forvejen
    private ProfessionalJob professionalJob;                                 // Stillingsbetegnelse  -  hvis der findes et match mellem listen i Nexus og stillingsteksten på det primære tilhørsforhold i SOFD, så udfyldes den
    private PrimaryOrganization primaryOrganization;                         // Primær organisation  -  hvis der er match mellem navnet på enheden fra SOFD (primært tilhørsforhold) og en værdi i Nexus listen, så vælges denne
    private Long defaultMedcomSenderOrganizationId;   						 // Default Medcom lokationsnummer  -  hvis der er match mellem navnet på enheden fra SOFD (primært tilhørsforhold) og en værdi i Nexus listen, så vælges denne
    private String identityId;												 // KMD Identity ID         - konfigurabelt om det sættes (if set, it will be set with UPN)
    
    // sættes kun ved oprettelse
    private KmdVagtplanConfiguration kmdVagtplanConfiguration;      // KMD Vagtplan ansættelsesforhold    -  hardkodet til "0"
    private String cpr;                                             // CPR
    private boolean replyToDefaultMedcomSenderOrganization;         // Anvend Default Medcom lokationsnummer ved besvarelse af Medcom-beskeder  (konfigurabel kommune-global indstilling)
    private ExchangeConfiguration exchangeConfiguration;            // Sæt hak i "Overfør kalenderbegivenheder til Exchange/outlook"  (konfigurabel kommune-global indstilling)
    private NationalRoleConfiguration nationalRoleConfiguration;    // Nationale roller, default blank, kan sættes til "Rettighed til Fælles Stamkort" eller "Rettighed til Fælles Stamkort og Aftaleoversigten" (konfigurabel kommune-global indstilling)
    private ActiveDirectoryConfiguration activeDirectoryConfiguration;       // UPN, opdateres med UPN fra den primære AD konto i SOFD (kræver at UPN læses ind i SOFD, hvilket er en ny ting)

    // sættes ikke, bruges bare til transport frem/tilbage
    private String color;
    private String basisReportUsername;
    private JsonNode smdbConfiguration;
    private JsonNode roadTimesCalculationConfiguration;
    // hack: by setting the type to JsonNode we just pass it back and forth, but ignore the content
//    private EventPlaningConfiguration eventPlaningConfiguration;
    private JsonNode eventPlaningConfiguration;
    private JsonNode sidConfigurationResource;
    private JsonNode patientBlackList;
}
