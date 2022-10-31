package dk.sofd.organization.ad.dao;

import org.springframework.data.repository.CrudRepository;

import dk.sofd.organization.ad.dao.model.Municipality;

public interface MunicipalityDao extends CrudRepository<Municipality, Long> {
	Municipality findByPassword(String password);
}