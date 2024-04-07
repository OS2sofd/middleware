package dk.digitalidentity.sofdcoreazureintegration.security;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofdcoreazureintegration.config.SofdCoreAzureIntegrationConfiguration;
import dk.digitalidentity.sofdcoreazureintegration.config.modules.AzureAd;
import dk.digitalidentity.sofdcoreazureintegration.dao.model.Municipality;
import dk.digitalidentity.sofdcoreazureintegration.util.Constants;

@Service
public class TokenService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SofdCoreAzureIntegrationConfiguration configuration;

    public BearerToken getToken(Municipality municipality) {
        // Use previous token if not about to expire
        if (municipality.getToken() != null && municipality.getToken().getExpiryTimestamp().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return municipality.getToken();
        }

        AzureAd adConfig = configuration.getAzureAd();

        // Url
        String url = adConfig.getLoginBaseUrl() + municipality.getTenantId() + "/oauth2/v2.0/token";

        // Create body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(Constants.Scope, "https://graph.microsoft.com/.default");
        body.add(Constants.GrantType, "client_credentials");
        body.add(Constants.ClientId, municipality.getClientId());
        body.add(Constants.ClientSecret, municipality.getClientSecret());

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        // Create and send request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});

        return new BearerToken(response.getBody());
    }
}