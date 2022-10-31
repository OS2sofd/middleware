package dk.sofd.opus.config;

import dk.sofd.opus.interceptor.RequestResponseLoggingInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Configuration
@Slf4j
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).setSSLSocketFactory(sslsf).build();

        BufferingClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpclient));

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));

        restTemplate.setErrorHandler(new ResponseErrorHandler() {

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().series() == CLIENT_ERROR || response.getStatusCode().series() == SERVER_ERROR;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                var errorMessage = "Rest Error (StatusCode: " + response.getRawStatusCode() + "; StatusText: " + response.getStatusText() + ")";
                if (response.getBody() != null) {
                    errorMessage += "\nResponse body:\n" + IOUtils.toString(response.getBody(), StandardCharsets.UTF_8.name());
                }

            	if (response.getRawStatusCode() != 404) {
	                log.error(errorMessage);
            	}
            	else {
            		log.warn(errorMessage);
            	}
            }
        });

        return restTemplate;
    }
}
