package dk.digitalidentity.sofd.logbuy.dao;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.sofd.logbuy.dao.model.CreatedPerson;

public interface CreatedPersonDao extends CrudRepository<CreatedPerson, Long> {
	CreatedPerson findByUuid(String uuid);
	List<CreatedPerson> findAll();
}