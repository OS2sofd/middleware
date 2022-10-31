package dk.sofd.core.stil.service.stil;

import dk.sofd.core.stil.dao.model.Municipality;
import https.wsieksport_unilogin_dk.eksport.fullmyndighed.InstitutionFullMyndighed;

public interface IStilService {
    InstitutionFullMyndighed getInstitution(String institutionCode, Municipality municipality);
}
