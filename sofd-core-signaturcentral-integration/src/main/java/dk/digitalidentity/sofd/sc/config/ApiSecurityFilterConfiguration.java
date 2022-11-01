package dk.digitalidentity.sofd.sc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.digitalidentity.sofd.sc.security.ApiSecurityFilter;
import dk.digitalidentity.sofd.sc.service.MunicipalityService;

@Configuration
public class ApiSecurityFilterConfiguration {

	@Autowired
	private MunicipalityService municipalityService;
	
	@Bean
	public FilterRegistrationBean<ApiSecurityFilter> apiSecurityFilter() {
		ApiSecurityFilter filter = new ApiSecurityFilter(municipalityService);

		FilterRegistrationBean<ApiSecurityFilter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
		filterRegistrationBean.addUrlPatterns("/api/*");
		filterRegistrationBean.setOrder(100);
		
		return filterRegistrationBean;
	}
}
