package dk.digitalidentity.brugertjek.dao.municipality;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
public class MunicipalityService {
	
	@Autowired
	private MunicipalityDao municipalityDao;

	@Cacheable(value = "municipalities")
	public Municipality findByCvr(String cvr) {
		return municipalityDao.findByCvr(cvr);
	}
	
	@CacheEvict(value = "municipalities", allEntries = true)
	public void cacheEvict() {
		;
	}

	public void save(Municipality municipality) {
		municipalityDao.save(municipality);
	}
}
