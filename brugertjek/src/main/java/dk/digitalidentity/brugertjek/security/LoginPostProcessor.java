package dk.digitalidentity.brugertjek.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.dao.municipality.MunicipalityService;
import dk.digitalidentity.samlmodule.model.SamlLoginPostProcessor;
import dk.digitalidentity.samlmodule.model.TokenUser;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginPostProcessor implements SamlLoginPostProcessor {
	private final SecurityUtil securityUtil;
	private final MunicipalityService municipalityService;

	@Override
	public void process(TokenUser tokenUser) {
		String cvr = tokenUser.getCvr();
		Municipality municipality = municipalityService.findByCvr(cvr);
		if (municipality == null) {
			throw new UsernameNotFoundException("Der er ikke opsat kontroller for CVR: " + cvr);
		}
		
		securityUtil.updateTokenUser(municipality, tokenUser);
	}
}
