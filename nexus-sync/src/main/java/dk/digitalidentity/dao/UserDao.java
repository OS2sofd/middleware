package dk.digitalidentity.dao;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDao extends JpaRepository<User, Long>  {
    List<User> findByMunicipality(Municipality municipality);

    User findByMunicipalityAndUserId(Municipality municipality, String userId);

	void deleteByFailedToCreateTrue();

	void deleteByMunicipalityAndId(Municipality municipality, long id);

	void deleteByMunicipalityAndNexusId(Municipality municipality, long id);
}
