package dk.digitalidentity.sofd.os2faktor.service.rc;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.os2faktor.dao.model.Municipality;

@Service
public class RoleCatalogueService {

	@Autowired
	private RestTemplate restTemplate;

	public List<RcUser> getUsersWithRoleWithRoleId(Municipality municipality, String roleId) throws Exception {
		String url = municipality.getRoleCatalogUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}

		url += "api/read/assigned/" + roleId + "?indirectRoles=true";

		HttpHeaders headers = new HttpHeaders();
		headers.add("ApiKey", municipality.getRoleCatalogApiKey());

		HttpEntity<RoleWrapperDTO> request = new HttpEntity<>(headers);

		ResponseEntity<RoleWrapperDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<RoleWrapperDTO>() { });
		if (response.getStatusCodeValue() != 200) {
			throw new Exception(municipality.getName() + ": Could not call OS2rollekatalog - http " + response.getStatusCodeValue());
		}

		return response.getBody().getAssignments();
	}

	public List<UserRole> getUserRolesFromItSystem(Municipality municipality, int itSystemIdentifier) throws Exception {
		String url = municipality.getRoleCatalogUrl();
		if (!url.endsWith("/")) {
			url += "/";
		}

		url += "api/read/itsystem/" + itSystemIdentifier + "?indirectRoles=true";

		HttpHeaders headers = new HttpHeaders();
		headers.add("ApiKey", municipality.getRoleCatalogApiKey());

		HttpEntity<UserRole[]> request = new HttpEntity<>(headers);

		ResponseEntity<UserRole[]> response = restTemplate.exchange(url, HttpMethod.GET, request, UserRole[].class);
		if (response.getStatusCodeValue() != 200 || response.getBody() == null) {
			throw new Exception(municipality.getName() + ": Could not call OS2rollekatalog - http " + response.getStatusCodeValue());
		}

		return Arrays.asList(response.getBody());
	}
}
