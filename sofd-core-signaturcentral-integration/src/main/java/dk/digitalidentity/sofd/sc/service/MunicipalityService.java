package dk.digitalidentity.sofd.sc.service;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofd.sc.config.SofdConfiguration;
import dk.digitalidentity.sofd.sc.dao.MunicipalityDao;
import dk.digitalidentity.sofd.sc.dao.model.Municipality;

@EnableCaching
@EnableScheduling
@Service
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;
	
	@Autowired
	private SofdConfiguration sofdConfiguration;

	@SuppressWarnings("deprecation")
	@PostConstruct
	private void init() {
		//Migration of municipalities from application.properties to database
		if (municipalityDao.findAll().isEmpty()) {
			sofdConfiguration.getMunicipalities().forEach(m -> {
				Municipality municipality = new Municipality();
				municipality.setName(m.getName());
				municipality.setPassword(m.getPassword());
				municipality.setSofdUrl(m.getSofdUrl());
				municipality.setSofdApiKey(m.getSofdApiKey());

				municipalityDao.save(municipality);
			});
		}
	}

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
