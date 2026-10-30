package dk.digitalidentity.brugertjek.controller.mvc.validation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dk.digitalidentity.brugertjek.config.BrugertjekConfiguration;
import dk.digitalidentity.brugertjek.dao.auditlog.AuditLogService;
import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class ValidationController {
	private final SecurityUtil securityUtil;
	private final AuditLogService auditLogService;
	private final MunicipalityService municipalityService;
	private final BrugertjekConfiguration configuration;

	@GetMapping("/validate")
	public String validate(Model model, @RequestParam(name = "userId", required = false) String userId, HttpServletRequest request, HttpSession session) {
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());
		if (municipality == null) {
			throw new RuntimeException("Kan ikke finde konfiguration for CVR: " + securityUtil.getCvr());
		}

		session.removeAttribute(Constants.KOMBIT_UUID);
		session.removeAttribute(Constants.USER_ID);
		session.removeAttribute(Constants.UPN);

		if (securityUtil.isSupporter()) {
			if (StringUtils.hasText(userId)) {
				// validate someone else
				session.setAttribute(Constants.USER_ID, userId);

				auditLogService.logValidation(userId);
			}
			else {
				// who do you want to validate?
				return "redirect:/supporter";
			}
		}
		else if (municipality.isAllowUserLogin()) {
			// validate yourself and fallthrough
			session.setAttribute(Constants.KOMBIT_UUID, securityUtil.getKombitUuid());
			
			model.addAttribute("findUserIdFirst", true);
		}
		else {
			// you should not be allowed to go here
			return "redirect:/";
		}

		model.addAttribute("adEnabled", configuration.getDevMode().isAdDevMode() || municipality.isAdEnabled());
		model.addAttribute("sofdEnabled", configuration.getDevMode().isSofdDevMode() || municipality.isOS2sofdEnabled());
		model.addAttribute("nexusEnabled", configuration.getDevMode().isNexusDevMode() || municipality.isNexusEnabled());
		model.addAttribute("rcEnabled", configuration.getDevMode().isRcDevMode() || municipality.isRolecatalogEnabled());
		model.addAttribute("os2faktorEnabled", configuration.getDevMode().isOs2faktorDevMode() || municipality.isOS2faktorEnabled());
		model.addAttribute("entraEnabled", municipality.isEntraEnabled());

		return "validation";
	}
}
