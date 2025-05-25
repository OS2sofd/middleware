package dk.digitalidentity.dao;

import dk.digitalidentity.dao.model.MunicipalitySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipalitySettingsDao extends JpaRepository<MunicipalitySettings, Long> {
		MunicipalitySettings findByCvr(String cvr);
}
