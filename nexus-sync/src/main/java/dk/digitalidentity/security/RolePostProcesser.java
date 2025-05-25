package dk.digitalidentity.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.samlmodule.model.SamlGrantedAuthority;
import dk.digitalidentity.samlmodule.model.SamlLoginPostProcessor;
import dk.digitalidentity.samlmodule.model.TokenUser;
import dk.digitalidentity.service.MunicipalityService;

@Component
public class RolePostProcesser implements SamlLoginPostProcessor {

	@Value("${config.dev:false}")
	private boolean dev;
	
	@Autowired
	private MunicipalityService municipalityService;
	
	@Override
	public void process(TokenUser tokenUser) {
		String cvr = tokenUser.getCvr();
		if (dev) {
			cvr = "12345678";
		}

		Municipality municipality = municipalityService.getByCvr(cvr);
		if (municipality == null) {
			throw new UsernameNotFoundException("Ukendt CVR: " + cvr);
		}

		List<SamlGrantedAuthority> newAuthorities = new ArrayList<>();

		for (Iterator<? extends SamlGrantedAuthority> iterator = tokenUser.getAuthorities().iterator(); iterator.hasNext();) {
			SamlGrantedAuthority grantedAuthority = iterator.next();

			if ("ROLE_http://nexussync.digital-identity.dk/roles/usersystemrole/useradmin/1".equals(grantedAuthority.getAuthority())) {
				newAuthorities.add(new SamlGrantedAuthority("ROLE_ADMIN", null, null));
				newAuthorities.add(new SamlGrantedAuthority("ROLE_USER", null, null));
			}
			if ("ROLE_http://nexussync.digital-identity.dk/roles/usersystemrole/user/1".equals(grantedAuthority.getAuthority())) {
				newAuthorities.add(new SamlGrantedAuthority("ROLE_USER", null, null));
			}
		}

		if (dev) {
			newAuthorities.add(new SamlGrantedAuthority("ROLE_ADMIN", null, null));
			newAuthorities.add(new SamlGrantedAuthority("ROLE_USER", null, null));
		}

		if (newAuthorities.isEmpty()) {
			throw new UsernameNotFoundException("Ingen tildelte roller");
		}

		tokenUser.setAuthorities(newAuthorities);
	}
}
