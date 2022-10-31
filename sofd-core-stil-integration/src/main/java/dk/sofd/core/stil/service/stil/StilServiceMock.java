package dk.sofd.core.stil.service.stil;

import https.unilogin_dk.data.Address;
import https.unilogin_dk.data.transitional.Employee;
import https.unilogin_dk.data.transitional.UniLoginFull;
import https.wsieksport_unilogin_dk.eksport.fullmyndighed.InstitutionFullMyndighed;
import https.wsieksport_unilogin_dk.eksport.fullmyndighed.InstitutionPersonFullMyndighed;
import https.wsieksport_unilogin_dk.eksport.fullmyndighed.PersonFullMyndighed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dk.sofd.core.stil.dao.model.Municipality;

@Slf4j
@Service
@Profile("StilServiceMock")
public class StilServiceMock implements IStilService {

    @Override
    public InstitutionFullMyndighed getInstitution(String institutionCode, Municipality municipality) {
        var result = new InstitutionFullMyndighed();
        result.setInstitutionNumber(institutionCode);
        var testMyndighedPerson = new InstitutionPersonFullMyndighed();
        var testUnilogin = new UniLoginFull();
        testUnilogin.setCivilRegistrationNumber("0101010101");
        testUnilogin.setUserId("stil001");
        testMyndighedPerson.setUNILogin(testUnilogin);
        var testPerson = new PersonFullMyndighed();
        testPerson.setFirstName("Stil");
        testPerson.setFamilyName("Tester");
        var testAddress = new Address();
        testAddress.setStreetAddress("Testvej 1");
        testAddress.setPostalDistrict("Testby");
        testAddress.setPostalCode("9999");
        testPerson.setAddress(testAddress);
        testMyndighedPerson.setPerson(testPerson);
        var testEmployee = new Employee();
        testMyndighedPerson.setEmployee(testEmployee);
        result.getInstitutionPerson().add(testMyndighedPerson);
        return result;
    }
}