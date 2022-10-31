package dk.sofd.core.stil.dao;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.sofd.core.stil.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	List<Municipality> findAll();
}