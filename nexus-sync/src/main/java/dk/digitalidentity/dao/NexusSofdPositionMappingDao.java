package dk.digitalidentity.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dk.digitalidentity.dao.model.NexusSofdPositionMapping;

public interface NexusSofdPositionMappingDao extends JpaRepository<NexusSofdPositionMapping, Long>  {
    List<NexusSofdPositionMapping> findByCvr(String cvr);
    NexusSofdPositionMapping findByCvrAndSofdPositionIgnoreCase(String cvr, String sofdPosition);
}
