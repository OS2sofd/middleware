package dk.digitalidentity.brugertjek.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class OpsApi {
	
	@Autowired
	private MunicipalityService municipalityService;
	
	@GetMapping("/ops/clearMunicipalityCache")
	public ResponseEntity<?> evictMunicipalityCache() {
		municipalityService.cacheEvict();

		log.info("Municipality cache cleared");

		return ResponseEntity.ok("");
	}
}
