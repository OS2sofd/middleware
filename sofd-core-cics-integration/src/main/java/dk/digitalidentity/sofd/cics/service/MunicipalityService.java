package dk.digitalidentity.sofd.cics.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofd.cics.config.SofdConfiguration;
import dk.digitalidentity.sofd.cics.dao.MunicipalityDao;
import dk.digitalidentity.sofd.cics.dao.model.Municipality;

@EnableCaching
@EnableScheduling
@Service
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;
	
	@Autowired
	private SofdConfiguration sofdConfiguration;
	
	@Autowired
	private MunicipalityService self;

	@SuppressWarnings("deprecation")
	@PostConstruct
	private void init() {
		// migration of municipalities from application.properties to database
		if (municipalityDao.findAll().isEmpty()) {
			sofdConfiguration.getMunicipalities().forEach(m -> {
				Municipality municipality = new Municipality();
				municipality.setName(m.getName());
				municipality.setCicsKeystore(m.getCicsKeystore());
				municipality.setCicsPassword(m.getCicsPassword());
				municipality.setCicsLosId(m.getCicsLosId());
				municipality.setSofdUrl(m.getSofdUrl());
				municipality.setSofdApiKey(m.getSofdApiKey());
				municipality.setAccountOrdersEnabled(m.isAccountOrdersEnabled());

				municipalityDao.save(municipality);
			});
		}
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void clearMunicipalitiesCache() {
		self.clearCache();
	}

	@CacheEvict(value = "municipalities", allEntries = true)
	public void clearCache() {
		
	}

	@Cacheable("municipalities")
	public List<Municipality> findAll() {
		return municipalityDao.findAll();
	}

}
