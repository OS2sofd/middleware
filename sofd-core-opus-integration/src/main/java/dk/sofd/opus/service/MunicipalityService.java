package dk.sofd.opus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dk.sofd.opus.dao.MunicipalityDao;
import dk.sofd.opus.dao.model.Municipality;

@EnableCaching
@EnableScheduling
@Service
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;
	
	@Scheduled(fixedRate = 1 * 60 * 1000)
	@CacheEvict(value = "municipalities", allEntries = true)
	public void clearMunicipalitiesCache() {
		// Clear cache
	}

	@Cacheable("municipalities")
	public List<Municipality> findAll() {
		return municipalityDao.findAll();
	}
}
