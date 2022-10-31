package dk.sofd.opus.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.sofd.opus.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	List<Municipality> findAll();
}
