package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.rc.RoleCatalogValidationService;
import dk.digitalidentity.brugertjek.integration.rc.RoleCatalogueRolesResponse;
import dk.digitalidentity.brugertjek.integration.rc.RoleDTO;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class OS2rollekatalogValidationController {
	private final RoleCatalogValidationService validationService;
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;

	@GetMapping("/validate/rc")
	public String validateRolecatalog(Model model, HttpServletRequest request, HttpSession session) {
		Map<String, String> userRoles = null;
		boolean disabled = false;
		
		String userId = (String) session.getAttribute(Constants.USER_ID);
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());

		if (userId != null && municipality != null) {
			RoleCatalogueRolesResponse response = validationService.getRoles(municipality, userId);
			if (response != null) {
				disabled = response.isDisabled();
				userRoles = response.getRoleMap();
			}
		}

		List<Validation> validations = validationService.validate(municipality, userId, userRoles, disabled);
		
		// make UI happy
		if (userRoles == null) {
			userRoles = new HashMap<String, String>();
		}
		
		List<RoleDTO> userRolesDTO = userRoles.entrySet().stream().map(e -> new RoleDTO(e.getValue())).toList();
		
		model.addAttribute("validations", validations);
		model.addAttribute("userRoles", userRolesDTO);
		model.addAttribute("disabled", disabled);
		
		return "user/result-rc :: result";
	}
}
