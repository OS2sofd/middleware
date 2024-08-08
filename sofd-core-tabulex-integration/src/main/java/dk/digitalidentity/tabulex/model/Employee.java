package dk.digitalidentity.tabulex.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private int id;
    private List<String> roller;
    private String initialer;
    private int institutionstype;
    private String loenkode;
    private int kommunekode;
    private String stillingsbetegnelse;
    private String startDato;
    private String slutDato;
    private String cpr;
    private String skolekode;
    private String aliasFornavn;
    private String aliasEfternavn;
    private String afdelingId;
    private String afdelingNavn;
    
	public String getMaskedCpr() {
		if (cpr == null) {
			return null;
		}
		
		if (cpr.length() != 10) {
			return cpr;
		}
		
		return cpr.substring(0, 6) + "-XXXX";
	}
}
