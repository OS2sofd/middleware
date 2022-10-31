package dk.sofd.organization.ad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import dk.sofd.organization.ad.dao.MunicipalityDao;
import dk.sofd.organization.ad.dao.model.Municipality;

@EnableCaching
@Service
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;
	
	@Cacheable("municipality")
	public Municipality findByPassword(String apiKey) {
		return municipalityDao.findByPassword(apiKey);
	}
}
