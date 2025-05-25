package dk.digitalidentity.service;

import dk.digitalidentity.dao.MunicipalitySettingsDao;
import dk.digitalidentity.dao.model.MunicipalitySettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MunicipalitySettingsService {

	@Autowired
	private MunicipalitySettingsDao municipalitySettingsDao;

	public MunicipalitySettings findByCvr(String cvr) {
		return municipalitySettingsDao.findByCvr(cvr);
	}

	public void save(MunicipalitySettings municipalitySettings) {
		municipalitySettingsDao.save(municipalitySettings);
	}
}
