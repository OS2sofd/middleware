package dk.digitalidentity.brugertjek.controller.mvc.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.integration.sofd.Affiliation;
import dk.digitalidentity.brugertjek.integration.sofd.Person;
import dk.digitalidentity.brugertjek.integration.sofd.PersonDTO;
import dk.digitalidentity.brugertjek.integration.sofd.SofdUser;
import dk.digitalidentity.brugertjek.integration.sofd.SofdValidationService;
import dk.digitalidentity.brugertjek.security.RequireAnyRole;
import dk.digitalidentity.brugertjek.security.SecurityUtil;
import dk.digitalidentity.brugertjek.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequireAnyRole
public class OS2sofdValidationController {
	private final SofdValidationService validationService;
	private final MunicipalityService municipalityService;
	private final SecurityUtil securityUtil;
	
	@GetMapping("/validate/sofd")
	public String validateSOFD(Model model, HttpServletRequest request, HttpSession session) {
		Person person = null;
		
		String userId = (String) session.getAttribute(Constants.USER_ID);
		String kombitUuid = (String) session.getAttribute(Constants.KOMBIT_UUID);
		String cvr = securityUtil.getCvr();
		Municipality municipality = municipalityService.findByCvr(cvr);

		if ((userId != null || kombitUuid != null) && municipality != null) {
			person = validationService.getPerson(municipality, userId, kombitUuid);
			
			// extract correct userId if not supplied
			if (userId == null) {
				if (person != null && person.getUsers() != null) {
					SofdUser sofdUser = person.getUsers().stream()
							.filter(u -> Objects.equals(u.getKombitUuid(), kombitUuid))
							.findFirst()
							.orElse(null);
					
					if (sofdUser != null) {
						userId = sofdUser.getUserId();
						session.setAttribute(Constants.USER_ID, userId);
					}
				}
			}
		}

		List<Validation> validations = validationService.validate(municipality, userId, person);

		if (person != null) {
			String fUserId = (userId != null) ? userId.toLowerCase() : null;

			SofdUser user = person.getUsers().stream()
				.filter(u -> Objects.equals(u.getUserId().toLowerCase(), fUserId) && Objects.equals(u.getUserType(), "ACTIVE_DIRECTORY"))
				.findAny()
				.orElse(null);

			if (user != null) {
				session.setAttribute(Constants.KOMBIT_UUID, user.getKombitUuid());
			}
		}
		else {
			// make UI happy
			person = new Person();
		}

		// store affiliations on Session for comparison in OS2sync validation
		session.removeAttribute(Constants.AFFILIATIONS);
		if (person.getAffiliations() != null && person.getAffiliations().size() > 0) {
			String now = LocalDate.now().toString();

			// only care about active affiliations
			List<Affiliation> affiliations = person.getAffiliations().stream()
				.filter(a -> a.getStartDate().compareTo(now) <= 0 && (!StringUtils.hasText(a.getStopDate()) || a.getStopDate().compareTo(now) >= 0))
				.collect(Collectors.toList());
			
			if (affiliations.size() > 0) {
				session.setAttribute(Constants.AFFILIATIONS, affiliations);
			}
		}
		
		model.addAttribute("validations", validations);
		model.addAttribute("showCPR", securityUtil.isCprAccess());
		model.addAttribute("sofdPerson", new PersonDTO(userId, person));

		return "user/result-sofd :: result";
	}
}
