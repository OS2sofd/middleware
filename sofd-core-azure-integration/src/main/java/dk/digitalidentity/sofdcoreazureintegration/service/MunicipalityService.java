package dk.digitalidentity.sofdcoreazureintegration.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofdcoreazureintegration.dao.MunicipalityDao;
import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;

@Service
public class MunicipalityService {

	@Autowired
	private MunicipalityDao municipalityDao;

	public List<Municipality> findAll() {
		return municipalityDao.findAll();
	}
}
