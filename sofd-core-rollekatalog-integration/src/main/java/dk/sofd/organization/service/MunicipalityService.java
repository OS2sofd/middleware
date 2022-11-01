package dk.sofd.organization.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.sofd.organization.dao.MunicipalityDao;
import dk.sofd.organization.dao.model.Municipality;

@Service
public class MunicipalityService {

	@Autowired
	private MunicipalityDao municipalityDao;

	public List<Municipality> findAll() {
		return municipalityDao.findAll();
	}
}
