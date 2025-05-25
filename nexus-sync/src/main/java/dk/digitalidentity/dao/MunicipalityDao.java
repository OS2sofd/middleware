package dk.digitalidentity.dao;

import dk.digitalidentity.dao.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipalityDao extends JpaRepository<Municipality, Long>  {

	Municipality findByCvr(String cvr);

}
