package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.os2sync.OS2SyncValidationService;
import dk.digitalidentity.brugertjek.integration.os2sync.User;
import dk.digitalidentity.brugertjek.integration.sofd.Affiliation;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class OS2syncValidationController {
	private final OS2SyncValidationService validationService;
	private final SecurityUtil securityUtil;
	private final MunicipalityService municipalityService;

	@SuppressWarnings("unchecked")
	@GetMapping("/validate/os2sync")
	public String validateOS2Sync(Model model, HttpServletRequest request, HttpSession session) {
		User user = null;
		String kombitUuid = (String) session.getAttribute(Constants.KOMBIT_UUID);
		
		List<Affiliation> affiliations = null;
		Object o = session.getAttribute(Constants.AFFILIATIONS);
		if (o != null && o instanceof List<?>) {
			affiliations = (List<Affiliation>) o;
		}
		
		String cvr = securityUtil.getCvr();
		Municipality municipality = municipalityService.findByCvr(cvr);
		if (municipality != null && kombitUuid != null) {
			user = validationService.getUser(municipality, kombitUuid);
		}

		List<Validation> validations = validationService.validate(municipality, kombitUuid, user, affiliations);
		
		// make UI easier with an empty person
		if (user == null) {
			user = new User();
		}

		model.addAttribute("validations", validations);
		model.addAttribute("user", user);
		
		return "user/result-os2sync :: result";
	}
}
