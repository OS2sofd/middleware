package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.ad.ActiveDirectoryValidationService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class ActiveDirectoryValidationController {
	private final ActiveDirectoryValidationService validationService;
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;

	@GetMapping("/validate/ad")
	public String validateActiveDirectory(Model model, HttpServletRequest request, HttpSession session) {
		Map<String, String> attributes = new HashMap<>();
		
		String userId = (String) session.getAttribute(Constants.USER_ID);		
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());
		if (municipality != null) {
			attributes = validationService.getAttributes(municipality, userId);
		}
		
		List<Validation> validationsList = validationService.validate(municipality, userId, attributes);
		
		// void NPEs from here on
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}

		// store UPN for EntraID lookup
		if (attributes.keySet().contains("userPrincipalName")) {
			session.setAttribute(Constants.UPN, attributes.get("userPrincipalName"));
		}

		if (!securityUtil.isCprAccess()) {
			if (attributes.keySet().contains(municipality.getAdCprfield())) {
				attributes.remove(municipality.getAdCprfield());
			}
		}
		
		model.addAttribute("validations", validationsList);
		model.addAttribute("attributes", attributes);
		
		return "user/result-ad :: result";
	}
}
