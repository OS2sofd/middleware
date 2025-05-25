package dk.digitalidentity.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.security.RequireUserAccess;
import dk.digitalidentity.security.SecurityUtil;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.UserService;

@Controller
@RequireUserAccess
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private MunicipalityService municipalityService;
	
	@GetMapping("/users")
	public String users(Model model) {
		Municipality municipality = municipalityService.getByCvr(SecurityUtil.getCvr());
		
		model.addAttribute("users", userService.getByMunicipalityIncludingFailures(municipality));
		
		return "users/list";
	}
}
