package dk.digitalidentity.sofdcoreazureintegration.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;

public interface MunicipalityDao extends JpaRepository<Municipality, Long> {
	List<Municipality> findAll();
}