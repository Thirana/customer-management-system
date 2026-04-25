package com.example.customermanagement.config;

import com.example.customermanagement.constant.ApiConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Keep CORS limited to API routes; Swagger/static resources do not need browser writes.
        registry.addMapping("/api/**")
                .allowedOrigins(appProperties.getFrontend().getAllowedOrigin())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(ApiConstants.REQUEST_ID_HEADER)
                .allowCredentials(true);
    }
}
