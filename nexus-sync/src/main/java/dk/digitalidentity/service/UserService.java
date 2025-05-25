package dk.digitalidentity.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.dao.UserDao;
import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.User;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public List<User> getByMunicipalityIncludingFailures(Municipality municipality) {
        return userDao.findByMunicipality(municipality);
    }

    public List<User> getByMunicipality(Municipality municipality) {
        List<User> users = userDao.findByMunicipality(municipality);
        
        // remove all failed create
        return users.stream().filter(u -> !u.isFailedToCreate()).collect(Collectors.toList());
    }

    public User save(User user) {
        if (user.getId() == 0) {
        	user.setCreated(LocalDateTime.now());
        }

        return userDao.save(user);
    }

    public User getByMunicipalityAndUserId(Municipality municipality, String userId) {
        return userDao.findByMunicipalityAndUserId(municipality, userId);
    }

	public void saveAll(List<User> users) {
		for (User user : users) {
	        if (user.getId() == 0) {
	        	user.setCreated(LocalDateTime.now());
	        }
		}

		userDao.saveAll(users);
	}

	@Transactional
	public void deleteFailedUsersFromCache() {
		userDao.deleteByFailedToCreateTrue();
	}

	@Transactional
	public void deleteByMunicipalityAndId(Municipality municipality, long id) {
		userDao.deleteByMunicipalityAndId(municipality, id);
	}

	@Transactional
	public void deleteByMunicipalityAndNexusId(Municipality municipality, long id) {
		userDao.deleteByMunicipalityAndNexusId(municipality, id);
	}
}
