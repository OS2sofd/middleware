package dk.digitalidentity.brugertjek.integration.entra;

import java.util.List;

import org.springframework.stereotype.Service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.LicenseDetails;
import com.microsoft.graph.models.LicenseDetailsCollectionResponse;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
class EntraService {
	private GraphServiceClient graphServiceClient;

	EntraUserLookup getStatus(final Municipality municipality, final String userId) throws EntraException {
		this.graphServiceClient = createGraphClient(municipality);

		final User user = getUser(municipality, userId);
		if (user == null) {
			return null;
		}

		final boolean userActive = getUserActiveStatus(user);
		final List<String> licenses = getLicenseDetails(user).stream()
			.map(ld -> ld.getSkuPartNumber())
			.toList();

		return new EntraUserLookup(userActive, licenses);
	}

	private User getUser(final Municipality municipality, final String userId) throws EntraException {
		try {
			return graphServiceClient.users()
				.byUserId(userId)
				.get(req -> req.queryParameters.select = new String[] { "id", "accountEnabled" });
		}
		catch (final NullPointerException ex) {
			// it throws NPE when the user does not exist :(
			log.warn(municipality.getName() + " - could not find " + userId);
		}
		catch (final ApiException ex) {
			log.warn(municipality.getName() + " - api lookup error on " + userId, ex);
		}
		
		return null;
	}

	private boolean getUserActiveStatus(User user) throws EntraException {
		try {
			if (user.getAccountEnabled() == null) {
				throw new EntraException("Kunne ikke få bruger aktiv status");
			}

			return user.getAccountEnabled().booleanValue();
		}
		catch (final ApiException ex) {
			throw new EntraException(ex.getMessage());
		}
	}

	private List<LicenseDetails> getLicenseDetails(User user) throws EntraException {
		try {
			final LicenseDetailsCollectionResponse licenseDetailsResponse = graphServiceClient
				.users()
				.byUserId(user.getId())
				.licenseDetails()
				.get();

			return licenseDetailsResponse.getValue();
		}
		catch (final ApiException ex) {
			throw new EntraException(ex.getMessage());
		}
	}

	private GraphServiceClient createGraphClient(Municipality municipality) {
		final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
			.clientId(municipality.getEntraClientId())
			.tenantId(municipality.getEntraTenantId())
			.clientSecret(municipality.getEntraClientSecret())
			.build();

		final String[] scopes = new String[] { "https://graph.microsoft.com/.default" };

		return new GraphServiceClient(clientSecretCredential, scopes);
	}
}
