package dk.digitalidentity.security;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dk.digitalidentity.samlmodule.model.SamlGrantedAuthority;
import dk.digitalidentity.samlmodule.model.TokenUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityUtil {
	private static boolean dev;

	@Value("${config.dev:false}")
    public void setDev(boolean devValue) {
		SecurityUtil.dev = devValue;
    }

	public static String getCvr() {
		String cvr = null;

		if (dev) {
			return "12345678";
		}

		if (isUserLoggedIn()) {
			cvr = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getCvr();
		}

		return cvr;
	}

	public static String getUser() {
		String uuid = null;
		String name = null;

		if (isUserLoggedIn()) {
			try {
				String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				String[] split = principal.split(",");
				
				name = split[2].replace("CN=", "");
				uuid = split[split.length-1].replace("Serial=", "");
				
				if (uuid.length() != 36) {
					log.warn("UUID for logged in user is invalid: " + uuid);
				}
			}
			catch (Exception ex) {
				log.warn("Unable to parse username", ex);

				name = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				uuid = "";
			}
		}

		return name + " (" + uuid + ")";
	}
	
	public static boolean isUserLoggedIn() {
		return SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getDetails() != null
				&& SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof TokenUser;
	}
	
	
	@SuppressWarnings("unchecked")
	public static boolean hasRole(String role) {
        boolean hasRole = false;
        if (isUserLoggedIn()) {
            for (SamlGrantedAuthority authority : (List<SamlGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
                if (Objects.equals(authority.getAuthority(), role)) {
                    hasRole = true;
                    break;
                }
            }
        }

        return hasRole;
    }

	public static String getUserIP() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return "0.0.0.0";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return request.getRemoteAddr();
    }
}
