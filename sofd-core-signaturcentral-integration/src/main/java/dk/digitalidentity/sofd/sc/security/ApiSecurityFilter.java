package dk.digitalidentity.sofd.sc.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dk.digitalidentity.sofd.sc.dao.model.Municipality;
import dk.digitalidentity.sofd.sc.service.MunicipalityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiSecurityFilter implements Filter {
	private MunicipalityService municipalityService;

	public ApiSecurityFilter(MunicipalityService municipalityService) {
		this.municipalityService = municipalityService;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		try {
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
	
			// we are using a custom header instead of Authorization because the Authorization header plays very badly with the SAML filter
			String authHeader = request.getHeader("ApiKey");
			if (authHeader != null) {
				Municipality authenticatedMunicipality = null;

				for (Municipality municipality : municipalityService.findAll()) {
					if (municipality.getPassword().equals(authHeader)) {
						authenticatedMunicipality = municipality;
						break;
					}
				}

				if (authenticatedMunicipality == null) {
					unauthorized(response, "Invalid ApiKey header", authHeader);
					return;
				}

				MunicipalityHolder.set(authenticatedMunicipality);

				filterChain.doFilter(servletRequest, servletResponse);
			}
			else {
				unauthorized(response, "Missing ApiKey header", authHeader);
			}
		}
		finally {
			MunicipalityHolder.clear();
		}
	}

	private static void unauthorized(HttpServletResponse response, String message, String authHeader) throws IOException {
		log.warn(message + " (authHeader = " + authHeader + ")");
		response.sendError(401, message);
	}

	@Override
	public void destroy() {
		;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		;
	}
}