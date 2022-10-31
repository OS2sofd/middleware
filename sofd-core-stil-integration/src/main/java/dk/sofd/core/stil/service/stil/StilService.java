package dk.sofd.core.stil.service.stil;

import https.wsieksport_unilogin_dk.eksport.fullmyndighed.InstitutionFullMyndighed;
import https.wsieksport_unilogin_dk.ws.WsiEksportPortType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dk.sofd.core.stil.dao.model.Municipality;

@Slf4j
@Service
@Profile("!StilServiceMock")
public class StilService implements IStilService {

    @Autowired
    private WsiEksportPortType stilService;

    @Override
    public InstitutionFullMyndighed getInstitution(String institutionCode, Municipality municipality) {
        try {
            var response = stilService.eksporterXmlFuldMyndighed(municipality.getStilUsername(), municipality.getStilPassword(), institutionCode);
            return response.getUNILoginExportFullMyndighed().getInstitution();
        } catch (Exception e) {
            log.error("Failed to call with institutionCode " + institutionCode + " for " + municipality.getName() + ". " + e.getMessage());
            return null;
        }
    }
}