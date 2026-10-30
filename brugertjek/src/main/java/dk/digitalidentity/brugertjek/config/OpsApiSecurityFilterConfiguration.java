package dk.digitalidentity.brugertjek.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.digitalidentity.brugertjek.security.OpsApiSecurityFilter;

@Configuration
public class OpsApiSecurityFilterConfiguration {

	@Autowired
	private BrugertjekConfiguration configuration;
	
	@Bean(name="OpsApiSecurityFilter")
	public FilterRegistrationBean<OpsApiSecurityFilter> apiSecurityFilter() {
		OpsApiSecurityFilter filter = new OpsApiSecurityFilter(configuration.getOpsapi().getApikey());

		FilterRegistrationBean<OpsApiSecurityFilter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
		filterRegistrationBean.addUrlPatterns("/ops/*");
		filterRegistrationBean.setOrder(100);
		
		return filterRegistrationBean;
	}
}
