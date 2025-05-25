package dk.digitalidentity.service.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private long id;
    private boolean active = true;
    private int version;
    private String uid;
    private Links _links;
    
    // Dette er Profil skærmbilledet i Nexus

    // sættes ved oprettelse, og opdateres dagligt
    private String primaryIdentifier;   // Unik ID          -  sættes pt til brugernavnet på den primære AD konto
    private String initials;            // Initialer        -  konfigurabelt (enten AD brugernavn eller forbogstaver fra navn)
    private String organizationName;    // Organisation     -  navnet på den enhed deres primære tilhørsforhold peger på i SOFD
    private String unitName;            // Enhed            -  stillingsteksten på det primære tilhørsforhold i SOFD
    private ActiveDirectoryConfiguration activeDirectoryConfiguration;  // UPN, opdateres med UPN fra den primære AD konto i SOFD (kræver at UPN læses ind i SOFD, hvilket er en ny ting)
    
    // sættes kun ved oprettelse
    private String homeTelephone;       // Telefon (hjem)   -  primært telefonnummer fra SOFD
    private String mobileTelephone;     // Telefon (mobil)  -  primært telefonnummer fra SOFD
    private String firstName;           // Fornavn
    private String middleName;          // Mellemnavn
    private String lastName;            // Efternavn
    private String fullName;            //
    private String primaryEmailAddress; // ???              - primær email adresse fra SOFD
    private String departmentName;      // Afdeling         - hardkodet værdi, opsat i kommunens konfiguration af Digital Identity 
    private Long autosignatureId;       // Autosignatur     - sætte til "Standard"

    // vi udfylder Adresselinje 1, PostNr og Landekode
    private PrimaryAddress primaryAddress;

    // sættes ikke, bruges bare til transport frem/tilbage
    private String workTelephone;
    private String secondaryEmailAddres;
    private User user;
}
