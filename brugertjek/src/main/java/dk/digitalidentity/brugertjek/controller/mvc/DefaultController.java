package dk.digitalidentity.brugertjek.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.security.NoRoleRequired;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@NoRoleRequired
public class DefaultController {
	private final SecurityUtil securityUtil;

	// dashboard endpoint is just to force a login through SAML module
	@GetMapping({ "/", "/dashboard" })
	public String defaultPage(Model model, HttpServletRequest request, HttpSession session) {
		session.removeAttribute(Constants.USER_ID);

		if (securityUtil.isSupporter()) {
			return "redirect:/supporter";
		}
		else if (securityUtil.isAdmin()) {
			return "redirect:/admin";
		}
		else if (securityUtil.isUser()) {
			return "redirect:/validate";
		}

		return "index";
	}
	
	@GetMapping("/kontroller")
	public String kontroller(Model model) {
		return "kontroller";
	}
}
