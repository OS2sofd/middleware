package dk.digitalidentity.sofd.os2faktor.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofd.os2faktor.dao.MunicipalityDao;
import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;

@Service
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;

	public List<Municipality> findAll() {
		return municipalityDao.findAll();
	}
}
