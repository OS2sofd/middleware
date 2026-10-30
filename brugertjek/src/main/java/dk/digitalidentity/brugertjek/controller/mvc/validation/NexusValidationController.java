package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.nexus.EmployeeDTO;
import dk.digitalidentity.brugertjek.integration.nexus.NexusValidationService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class NexusValidationController {
	private final NexusValidationService validationService;
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;
	
	@GetMapping("/validate/nexus")
	public String validateNexus(Model model, HttpServletRequest request, HttpSession session) {
		EmployeeDTO employee = null;

		String userId = (String) session.getAttribute(Constants.USER_ID);
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());

		if (userId != null && municipality != null) {
			employee = validationService.getStatus(municipality, userId);
		}

		List<Validation> validations = validationService.validate(municipality, userId, employee);
		
		model.addAttribute("validations", validations);
		model.addAttribute("employee", employee);

		return "user/result-nexus :: result";
	}
}
