package dk.digitalidentity.brugertjek.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.util.Constants;
import dk.digitalidentity.samlmodule.model.SamlGrantedAuthority;
import dk.digitalidentity.samlmodule.model.TokenUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {

	public boolean isAdmin() {
		return hasRole(Constants.ROLE_ADMINISTRATOR);
	}

	public boolean isCprAccess() {
		return hasRole(Constants.ROLE_CPR_ACCESS);
	}

	public boolean isSupporter() {
		return hasRole(Constants.ROLE_SUPPORTER);
	}

	public boolean isUser() {
		return hasRole(Constants.ROLE_USER);
	}

	public String getKombitUuid() {
		String userId = getUserId();
		if (userId != null) {
			try {
				int idx = userId.indexOf(",Serial=");
				if (idx >= 0) {
					return userId.substring(idx + ",Serial=".length());
				}
			}
			catch (Exception ex) {
				log.warn("Cannot parse: " + userId);
			}
		}
		
		return null;
	}

	public String getUserId() {
		if (!isAuthenticated()) {
			return null;
		}

		TokenUser tokenUser = getTokenUser();
		if (tokenUser != null) {
			return tokenUser.getUsername();
		}

		return null;
	}
	
	public String getCvr() {
		if (!isAuthenticated()) {
			return null;
		}
		
		TokenUser tokenUser = getTokenUser();
		if (tokenUser != null) {
			return tokenUser.getCvr();
		}

		return null;
	}

	public String getUserIP() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return "0.0.0.0";
		}

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request.getRemoteAddr();
	}

	// Grant roles based on how SAML sends them to us
	public void updateTokenUser(Municipality municipality, TokenUser tokenUser) {
		List<SamlGrantedAuthority> authorities = new ArrayList<>();
		
		for (SamlGrantedAuthority authority : tokenUser.getAuthorities()) {
			if ("ROLE_http://brugertjek.dk/roles/usersystemrole/admin/1".equals(authority.getAuthority())) {
				authorities.add(new SamlGrantedAuthority(Constants.ROLE_ADMINISTRATOR));
			}
			
			if ("ROLE_http://brugertjek.dk/roles/usersystemrole/supporter/1".equals(authority.getAuthority())) {
				authorities.add(new SamlGrantedAuthority(Constants.ROLE_SUPPORTER));
			}
			
			if ("ROLE_http://brugertjek.dk/roles/usersystemrole/cpr/1".equals(authority.getAuthority())) {
				authorities.add(new SamlGrantedAuthority(Constants.ROLE_CPR_ACCESS));
			}
		}

		if (municipality.isAllowUserLogin()) {
			authorities.add(new SamlGrantedAuthority(Constants.ROLE_USER));
		}
		
		if (authorities.size() == 0) {
			throw new InsufficientAuthenticationException("Ingen roller tildelt til Brugertjek");
		}

		tokenUser.setAuthorities(authorities);
	}
	
	private TokenUser getTokenUser() {
		if (!isAuthenticated()) {
			return null;
		}

		return (TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
	}
	
	private boolean isAuthenticated() {
		return SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication().getDetails() != null &&
				SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof TokenUser;
	}

	private boolean hasRole(String role) {
		if (!isAuthenticated()) {
			return false;
		}

		return getTokenUser().getAuthorities().stream().anyMatch(ga -> Objects.equals(ga.getAuthority().toUpperCase(), role));
	}
}
