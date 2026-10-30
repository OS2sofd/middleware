package dk.digitalidentity.brugertjek.controller.mvc.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dk.digitalidentity.brugertjek.security.RequireSupporter;

@Controller
@RequireSupporter
public class SupporterController {

	record SearchForm(String searchText) { }

	@GetMapping("/supporter")
	public String admin(Model model) {
		model.addAttribute("searchForm", new SearchForm(""));
		
		return "supporter/search";
	}

	@PostMapping("/supporter/search")
	public String search(@ModelAttribute SearchForm form, Model model, BindingResult result, RedirectAttributes redirectAttributes) {
		if (form.searchText == null || !StringUtils.hasText(form.searchText)) {
			result.rejectValue("searchText", null, "Username cannot be empty.");
			return "supporter/search";
		}

		redirectAttributes.addAttribute("userId", form.searchText);

		return "redirect:/validate";
	}
}