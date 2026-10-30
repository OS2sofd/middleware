package dk.digitalidentity.brugertjek.controller.mvc.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.security.RequireAdminRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Controller
@RequireAdminRole
@RequiredArgsConstructor
public class AdminController {
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;

	@GetMapping("/admin")
	public String admin(Model model) throws Exception {
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());
		if (municipality == null) {
			throw new Exception("Kan ikke finde konfiguration for CVR: " + securityUtil.getCvr());
		}

		model.addAttribute("municipality", municipality);

		return "admin/settings";
	}

	@PostMapping("/admin")
	public String edit(@ModelAttribute Municipality form, Model model) throws Exception {
		Municipality municipality = municipalityService.findByCvr(securityUtil.getCvr());
		if (municipality == null) {
			throw new Exception("Kan ikke finde konfiguration for CVR: " + securityUtil.getCvr());
		}

		municipality.setAllowUserLogin(form.isAllowUserLogin());
		municipalityService.save(municipality);

		model.addAttribute("saved", true);
		model.addAttribute("municipality", municipality);

		return "admin/settings";
	}
}