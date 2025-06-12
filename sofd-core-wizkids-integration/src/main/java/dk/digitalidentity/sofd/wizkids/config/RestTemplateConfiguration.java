package dk.digitalidentity.sofd.wizkids.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    
    @SuppressWarnings("deprecation")
	@Bean
    public RestTemplate restTemplate() throws Exception {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslsf).build();
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec("standard").build()).setConnectionManager(connectionManager).build();

            BufferingClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpclient));

            RestTemplate restTemplate = new RestTemplate(requestFactory);

            return restTemplate;
    }

}
