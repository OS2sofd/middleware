package dk.sofd.organization.ad.service.model;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class Person {
    private String uuid;
    private String master;
    private boolean deleted;
    private String cpr;
    private String firstname;
    private String surname;
    private String chosenName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstEmploymentDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate anniversaryDate;
    
    private Set<User> users;
    private Set<Affiliation> affiliations;
	private Set<Phone> phones;
}
