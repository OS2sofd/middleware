package dk.digitalidentity.sofd.logbuy.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dk.digitalidentity.sofd.logbuy.dao.CreatedPersonDao;
import dk.digitalidentity.sofd.logbuy.dao.model.CreatedPerson;

@Service
public class CreatedPersonService {
	
	@Autowired
	private CreatedPersonDao createdPersonDao;
	
	public CreatedPerson getByUuid(String uuid) {
		return createdPersonDao.findByUuid(uuid);
	}
	
	public List<CreatedPerson> getAll() {
		return createdPersonDao.findAll();
	}
	
	public void delete(CreatedPerson person) {
		createdPersonDao.delete(person);
	}
	
	public CreatedPerson save(CreatedPerson person) {
		return createdPersonDao.save(person);
	}
}
