package dk.digitalidentity.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.User;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.UserService;

@RestController
public class SyncApi {

	@Autowired
	private UserService userService;
	
	@Autowired
	private MunicipalityService municipalityService;
	
	@PutMapping("/api/reset/{userId}/{cvr}")
	public ResponseEntity<String> resetUser(@PathVariable("userId") String userId, @PathVariable("cvr") String cvr) {
		Municipality municipality = municipalityService.getByCvr(cvr);
		if (municipality == null) {
			return ResponseEntity.notFound().build();
		}
		
		User user = userService.getByMunicipalityAndUserId(municipality, userId);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		
		userService.deleteByMunicipalityAndId(municipality, user.getId());
		
		return ResponseEntity.ok("");
	}
}
