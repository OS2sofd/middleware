package dk.digitalidentity.tabulex.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeDTO {
    private String cpr;
    private String skolekode;
    private String stillingsbetegnelse;
    private String aliasFornavn;
    private String aliasEfternavn;
    private String startDato;
    private String slutDato;
    private String afdelingId;
    private String afdelingNavn;
}
