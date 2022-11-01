package dk.digitalidentity.config;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

	@Autowired
	private SF1601PrintConfiguration config;

	@Bean
	public RestTemplate restTemplate() throws Exception {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		CloseableHttpClient client = null;
		if (config.getKeystore().getLocation() != null) {
			SSLContext sslContext = SSLContextBuilder.create()
			                .loadKeyMaterial(
			                		ResourceUtils.getFile(config.getKeystore().getLocation()),
			                		config.getKeystore().getPassword().toCharArray(),
			                		config.getKeystore().getPassword().toCharArray())
			                .loadTrustMaterial(acceptingTrustStrategy)
			                .build();
			
			client = HttpClients.custom()
				        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
						.setSSLContext(sslContext)
						.build();			
		}
		else {
			client = HttpClients.custom()
						.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
						.build();
		}

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectionRequestTimeout(3 * 60 * 1000);
		requestFactory.setConnectTimeout(3 * 60 * 1000);
		requestFactory.setReadTimeout(3 * 60 * 1000);
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

		return restTemplate;
	}
}
