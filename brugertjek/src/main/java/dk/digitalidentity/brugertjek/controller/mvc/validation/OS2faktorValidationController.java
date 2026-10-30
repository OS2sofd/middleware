package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.os2faktor.CoreDataExtendedLookup;
import dk.digitalidentity.brugertjek.integration.os2faktor.OS2faktorValidationService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class OS2faktorValidationController {
	private final OS2faktorValidationService validationService;
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;
	
	@GetMapping("/validate/os2faktor")
	public String validateOS2faktor(Model model, HttpServletRequest request, HttpSession session) {
		CoreDataExtendedLookup lookup = null;

		String userId = (String) session.getAttribute(Constants.USER_ID);
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());

		if (userId != null && municipality != null) {
			lookup = validationService.getStatus(municipality, userId);
		}

		List<Validation> validations = validationService.validate(municipality, userId, lookup);
		
		model.addAttribute("validations", validations);
		model.addAttribute("lookup", lookup);

		return "user/result-os2faktor :: result";
	}
}
