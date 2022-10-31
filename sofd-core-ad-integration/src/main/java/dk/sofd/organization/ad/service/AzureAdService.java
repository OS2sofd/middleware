package dk.sofd.organization.ad.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;

import dk.sofd.organization.ad.dao.model.Municipality;
import dk.sofd.organization.ad.service.model.AzureUser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

@EnableCaching
@Service
@Slf4j
public class AzureAdService {

	public GraphServiceClient<Request> initializeGraphAuth(Municipality municipality) throws ClientException {
		// Create the auth provider
		final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
				.tenantId(municipality.getAzureTenantId())
				.clientId(municipality.getAzureClientId())
				.clientSecret(municipality.getAzureSecret())
				.build();

		TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(credential);

		DefaultLogger defaultLogger = new DefaultLogger();

		// Build a Graph client
		return GraphServiceClient.builder()
				.authenticationProvider(authProvider)
				.logger(defaultLogger)
				.buildClient();
	}

	public Map<String, AzureUser> fetchAllAzureUsers(Municipality municipality) {
		// create a graphClient for the given municipality
		GraphServiceClient<Request> graphClient = initializeGraphAuth(municipality);

		// construct the first call to Microsoft Graph, additional calls (for pagination) will be handled by the api itself
		UserCollectionPage userCollectionPage = graphClient
				.users()
				.buildRequest()
				.select("userPrincipalName,id,onPremisesSamAccountName,onPremisesSyncEnabled,onPremisesUserPrincipalName")
				.filter("onPremisesSyncEnabled eq true")
				.top(999) // Max page size ?
				.get();

		// iterate over all users
		List<User> users = new ArrayList<>();
		while (userCollectionPage != null) {
			List<User> usersOnPage = userCollectionPage.getCurrentPage();
			users.addAll(usersOnPage);

			if (userCollectionPage.getNextPage() == null) {
				userCollectionPage = null;
			}
			else {
				userCollectionPage = userCollectionPage.getNextPage().buildRequest().get();
			}
		}



		// map returned users objects to a simple DTO with only the relevant fields
		var result = users
				.stream()
				.filter(u -> StringUtils.hasLength(u.onPremisesSamAccountName) && StringUtils.hasLength(u.onPremisesUserPrincipalName) && u.onPremisesUserPrincipalName.toLowerCase().endsWith(municipality.getAzureDomain().toLowerCase()))
				.map(u -> new AzureUser(u.userPrincipalName, u.id, u.onPremisesSamAccountName))
				.collect(Collectors.toMap(u -> u.getOnPremisesSamAccountName().toLowerCase(), Function.identity()));

		log.info("Azure user count after filtering: = " + result.size());
		return result;
	}

	public Map.Entry<String, AzureUser> fetchAzureUserById(GraphServiceClient<Request> graphClient, Municipality municipality, String userId) throws NullPointerException {
		Objects.requireNonNull(graphClient, "No Graph client provided");
		Objects.requireNonNull(municipality, "No municipality provided");

		List<Option> options = new LinkedList<>();
		options.add(new HeaderOption("ConsistencyLevel", "eventual")); // this is required if we want to search onPremisesSamAccountName

		// construct the first call to Microsoft Graph, additional calls (for pagination) will be handled by the api itself
		UserCollectionPage userCollectionPage = graphClient
				.users()
				.buildRequest(options)
				.select("userPrincipalName,id,onPremisesSamAccountName,onPremisesSyncEnabled,onPremisesUserPrincipalName")
				.filter("onPremisesSyncEnabled eq true and onPremisesSamAccountName eq '" + userId + "'")
				.top(999) // Max page size ?
				.count()
				.get();

		// iterate over all users
		List<User> users = new ArrayList<>();
		while (userCollectionPage != null) {
			List<User> usersOnPage = userCollectionPage.getCurrentPage();
			users.addAll(usersOnPage);

			if (userCollectionPage.getNextPage() == null) {
				userCollectionPage = null;
			}
			else {
				userCollectionPage = userCollectionPage.getNextPage().buildRequest().get();
			}
		}

		var azureUser = users.stream().filter(u -> StringUtils.hasLength(u.onPremisesSamAccountName) && StringUtils.hasLength(u.onPremisesUserPrincipalName) && u.onPremisesUserPrincipalName.toLowerCase().endsWith(municipality.getAzureDomain().toLowerCase())).findFirst();
		if( azureUser.isPresent() )
		{
			var user = azureUser.get();
			return Map.entry(user.onPremisesSamAccountName.toLowerCase(), new AzureUser(user.userPrincipalName.toLowerCase(), user.id, user.onPremisesSamAccountName));
		}
		return null;
	}
}
