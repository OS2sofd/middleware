package dk.sofd.opus.service.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgUnitManager
{
    private long id;
    private String name;
    private Person manager;
    private boolean inherited;
    private OrgUnit orgUnit;
}
