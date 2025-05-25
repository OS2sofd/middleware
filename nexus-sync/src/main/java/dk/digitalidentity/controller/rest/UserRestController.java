package dk.digitalidentity.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.security.RequireUserAccess;
import dk.digitalidentity.security.SecurityUtil;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequireUserAccess
public class UserRestController {

	@Autowired
	private UserService userService;

	@Autowired
	private MunicipalityService municipalityService;
	
	@PostMapping("/users/{id}/delete")
	public ResponseEntity<String> users(Model model, @PathVariable("id") long id) {
		Municipality municipality = municipalityService.getByCvr(SecurityUtil.getCvr());
		
		log.info(municipality.getName() + " : " + SecurityUtil.getUser() + " attempted to delete " + id);
		
		userService.deleteByMunicipalityAndId(municipality, id);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
