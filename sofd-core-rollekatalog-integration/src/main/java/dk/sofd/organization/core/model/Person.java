package dk.sofd.organization.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String personUuid;
    private String uuid;
    private String name;
    private String userId;
    private String email;
    private String phone;
    private String cpr;
    private String nemloginUserUuid;
    private boolean prime;
    private boolean doNotInherit;
    private boolean disabled;
    private List<Affiliation> affiliations;
    private List<String> klePrimary;
    private List<String> kleSecondary;
    private boolean schoolUser = false;
}
