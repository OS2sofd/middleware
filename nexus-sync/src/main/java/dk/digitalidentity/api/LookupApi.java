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
import dk.digitalidentity.service.nexus.NexusService;
import dk.digitalidentity.service.nexus.model.Employee;

@RestController
public class LookupApi {

	@Autowired
	private UserService userService;

	@Autowired
	private NexusService nexusService;

	@Autowired
	private MunicipalityService municipalityService;
	
	public record EmployeeDTO (long nexusId, boolean active, String nexusSyncStatus) { }

	@PutMapping("/api/lookup/{userId}/{cvr}")
	public ResponseEntity<EmployeeDTO> resetUser(@PathVariable("userId") String userId, @PathVariable("cvr") String cvr) {
		Municipality municipality = municipalityService.getByCvr(cvr);
		if (municipality == null) {
			return ResponseEntity.notFound().build();
		}
		
		Employee employee = nexusService.getEmployeeByUserId(municipality, userId);
		if (employee == null) {
			return ResponseEntity.notFound().build();
		}

		User user = userService.getByMunicipalityAndUserId(municipality, userId);
		if (user != null) {
			if (user.isFailedToCreate()) {
				return ResponseEntity.ok(new EmployeeDTO(employee.getId(), employee.isActive(), "Fejlet synkronisering"));
			}

			return ResponseEntity.ok(new EmployeeDTO(employee.getId(), employee.isActive(), "Synkroniseret"));
		}

		return ResponseEntity.ok(new EmployeeDTO(employee.getId(), employee.isActive(), "Findes ikke"));
	}
}
