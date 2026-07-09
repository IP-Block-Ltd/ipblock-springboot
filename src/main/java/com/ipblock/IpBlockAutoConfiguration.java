package com.ipblock;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Auto-configures the IP Block client and servlet filter.
 *
 * Active by default; disable with {@code ip-block.enabled=false}.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(IpBlockProperties.class)
@ConditionalOnProperty(prefix = "ip-block", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IpBlockAutoConfiguration {

    @Bean
    public IpBlockClient ipBlockClient(IpBlockProperties properties) {
        return new IpBlockClient(properties);
    }

    @Bean
    public FilterRegistrationBean<Filter> ipBlockFilterRegistration(
            IpBlockProperties properties, IpBlockClient client) {

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IpBlockFilter(properties, client));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("ipBlockFilter");
        return registration;
    }
}
