package dk.digitalidentity.sofd.wizkids.dao;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	List<Municipality> findAll();
}