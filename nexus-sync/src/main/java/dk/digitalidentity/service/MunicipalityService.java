package dk.digitalidentity.service;

import dk.digitalidentity.dao.MunicipalityDao;
import dk.digitalidentity.dao.model.Municipality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MunicipalityService {

    @Autowired
    private MunicipalityDao municipalityDao;

    public List<Municipality> getAll() {
        return municipalityDao.findAll();
    }

    public Municipality save(Municipality municipality) {
        return municipalityDao.save(municipality);
    }

	public Municipality getByCvr(String cvr) {
		return municipalityDao.findByCvr(cvr);
	}
}
