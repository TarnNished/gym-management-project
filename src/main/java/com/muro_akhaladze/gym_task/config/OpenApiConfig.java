package com.muro_akhaladze.gym_task.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI setup: adds the "Authorize" button so a JWT from POST /api/v1/auth/login
 * can be pasted once and is then sent with every "Try it out" request.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI gymOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym Management API")
                        .version("1.0")
                        .description("To call protected endpoints: log in with POST /api/v1/auth/login "
                                + "(demo user Liam.Anderson / password123), copy the token, "
                                + "click Authorize and paste it."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
