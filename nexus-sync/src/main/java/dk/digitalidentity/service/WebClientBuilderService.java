package dk.digitalidentity.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import dk.digitalidentity.dao.model.Municipality;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@EnableScheduling
@Service
public class WebClientBuilderService {
	private Map<String, WebClient> roleCatalogueWebClientMap = new HashMap<>();
	private Map<String, WebClient> nexusWebClientMap = new HashMap<>();
	
	// every 4 hours, we wipe the cached clients
	@Scheduled(fixedDelay = 4 * 60 * 60 * 1000)
	public void cleanupTask() {
		roleCatalogueWebClientMap = new HashMap<>();
		nexusWebClientMap = new HashMap<>();
	}
	
	public WebClient getNexusWebClient(Municipality municipality) {
		if (!nexusWebClientMap.containsKey(municipality.getCvr())) {
	         ClientRegistration registration = ClientRegistration
	                .withRegistrationId("nexus-" + municipality.getCvr())
	                .tokenUri(municipality.getNexusTokenUrl())
	                .clientId(municipality.getNexusClientId())
	                .clientSecret(municipality.getNexusClientSecret())
	                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
	                .build();
	        
	        ReactiveClientRegistrationRepository clientRegistrations = new InMemoryReactiveClientRegistrationRepository(registration);
	        
	        final ExchangeStrategies strategies = ExchangeStrategies.builder()
	                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(32 * 1024 * 1024))
	                .build();
	        
	        InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
	        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
	        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
	        oauth.setDefaultClientRegistrationId("nexus-" + municipality.getCvr());

	        HttpClient client = HttpClient.create()
	        		  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
	        		  .doOnConnected(conn -> conn.addHandlerFirst(new ReadTimeoutHandler(30)))
	        		  .responseTimeout(Duration.ofSeconds(10));

	        WebClient webClient = WebClient.builder()
	                .filter(oauth)
	                .clientConnector(new ReactorClientHttpConnector(client))
	                .exchangeStrategies(strategies)
	                .build();
	        
	        nexusWebClientMap.put(municipality.getCvr(), webClient);
    	}
    	
		return nexusWebClientMap.get(municipality.getCvr());
    }

	public WebClient getRoleCatalogueWebClient(Municipality municipality) {
		if (!roleCatalogueWebClientMap.containsKey(municipality.getCvr())) {
	        final ExchangeStrategies strategies = ExchangeStrategies.builder()
	                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(32 * 1024 * 1024))
	                .build();

	        HttpClient httpClient = HttpClient.create()
	        		  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
	        		  .doOnConnected(conn -> conn.addHandlerFirst(new ReadTimeoutHandler(60)))
	        		  .responseTimeout(Duration.ofSeconds(60));

	        WebClient webClient = WebClient.builder()
	                .clientConnector(new ReactorClientHttpConnector(httpClient))
	                .defaultHeader("apiKey", municipality.getRoleCatalogueApiKey())
	                .exchangeStrategies(strategies)
	                .build();
	        
	        roleCatalogueWebClientMap.put(municipality.getCvr(), webClient);
		}

		return roleCatalogueWebClientMap.get(municipality.getCvr());
    }
}
