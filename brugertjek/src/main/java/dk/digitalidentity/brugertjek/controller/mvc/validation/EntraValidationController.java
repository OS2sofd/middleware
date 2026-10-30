package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.entra.EntraUserLookup;
import dk.digitalidentity.brugertjek.integration.entra.EntraValidationService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
@Slf4j
public class EntraValidationController {
	private final MunicipalityService municipalityService;
	private final EntraValidationService entraValidationService;
	private final SecurityUtil securityUtil;

	@GetMapping("/validate/entra")
	public String validateEntra(Model model, HttpServletRequest request, HttpSession session) {
		EntraUserLookup lookup = null;
		
		String userId = (String) session.getAttribute(Constants.UPN);
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());

		if (userId != null || municipality != null) {
			lookup = entraValidationService.lookup(municipality, userId);
		}
		
		List<Validation> validations = entraValidationService.validate(municipality, userId, lookup);
		model.addAttribute("lookup", lookup);
		model.addAttribute("validations", validations);

		return "user/result-entra :: result";
	}
}
