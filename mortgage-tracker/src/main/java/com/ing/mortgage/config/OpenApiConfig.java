package com.ing.mortgage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ING Mortgage Payment Tracker API")
                        .version("1.0.0")
                        .description("MVP Backend service for tracking principal reductions and mortgage projections.")
                        .contact(new Contact()
                                .name("ING Backend Assignment Team")));
    }
}
