package dk.digitalidentity.sofd.cics.service;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;

@Component
public class RestTemplateFactory {
	private Map<String, RestTemplate> templateMap = new HashMap<>();
	
	public RestTemplate getRestTemplate(Municipality municipality) throws Exception {
		if (templateMap.containsKey(municipality.getName())) {
			return templateMap.get(municipality.getName());
		}

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		CloseableHttpClient client = null;
		SSLContext sslContext = SSLContextBuilder.create()
		                .loadKeyMaterial(ResourceUtils.getFile(municipality.getCicsKeystore()), municipality.getCicsPassword().toCharArray(), municipality.getCicsPassword().toCharArray())
		                .loadTrustMaterial(acceptingTrustStrategy)
		                .build();
		
		client = HttpClients.custom()
			        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
					.setSSLContext(sslContext)
					.build();			
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(client);

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}
			
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				;
			}
		});

		templateMap.put(municipality.getName(), restTemplate);
		
		return restTemplate;
	}
}
