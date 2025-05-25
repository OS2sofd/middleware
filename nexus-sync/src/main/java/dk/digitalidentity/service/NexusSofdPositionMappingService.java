package dk.digitalidentity.service;

import dk.digitalidentity.dao.NexusSofdPositionMappingDao;
import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.NexusSofdPositionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NexusSofdPositionMappingService {

    @Autowired
    private NexusSofdPositionMappingDao nexusSofdPositionMappingDao;

    public List<NexusSofdPositionMapping> getForMunicipality(Municipality municipality) {
        return nexusSofdPositionMappingDao.findByCvr(municipality.getCvr());
    }

    public NexusSofdPositionMapping getForMunicipalityAndSofdPosition(Municipality municipality, String sofdPosition) {
        return nexusSofdPositionMappingDao.findByCvrAndSofdPositionIgnoreCase(municipality.getCvr(), sofdPosition);
    }
}
