package dk.sofd.opus.service.model;

import java.util.Map;
import java.util.Set;

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
@EqualsAndHashCode(exclude = { "uuid" })
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Affiliation {
    private String uuid;
    private String personUuid;
    private String master;
    private String masterId;
    private String startDate;
    private String stopDate;
    private boolean deleted;
    private String orgUnitUuid;
    private String employeeId;
    private String employmentTerms;
    private String employmentTermsText;
    private String payGrade;
    private String wageStep;
    private Double workingHoursDenominator;
    private Double workingHoursNumerator;
    private String affiliationType;
    private String positionId;
    private String positionName;
    private String positionTypeId;
    private String positionTypeName;
    private Set<String> functions;
    private Set<String> managerForUuids;
    private Map<String, String> localExtensions;
}
