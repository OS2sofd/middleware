package dk.sofd.organization.dao;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.sofd.organization.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	List<Municipality> findAll();
}