package com.example.customermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customerManagementOpenApi() {
        // Endpoint-level details will be added on controllers in later phases.
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Management System API")
                        .version("v1")
                        .description("REST API for customer CRUD operations and Excel-based bulk import."));
    }
}
