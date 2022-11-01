package dk.digitalidentity.sofd.sc.dao;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.sofd.sc.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	List<Municipality> findAll();
}