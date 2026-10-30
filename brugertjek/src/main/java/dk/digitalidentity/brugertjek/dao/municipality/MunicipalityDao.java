package dk.digitalidentity.brugertjek.dao.municipality;

import org.springframework.data.jpa.repository.JpaRepository;

interface MunicipalityDao extends JpaRepository<Municipality, Long> {
	Municipality findByCvr(String cvr);
}
