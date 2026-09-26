package com.theatre.bookingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Central OpenAPI (Swagger) configuration for the Booking Service.
 *
 * <p>Exposes interactive API documentation at {@code /swagger-ui.html} and the raw
 * OpenAPI 3 definition at {@code /v3/api-docs}. All endpoints in this service require a
 * bearer (JWT) token, so the scheme is applied globally as a default security requirement.
 */
@Configuration
public class OpenApiConfig {

    /** Name of the reusable bearer-token security scheme referenced by secured operations. */
    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI bookingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking Service API")
                        .description("Creation, lookup and cancellation of theatre bookings for the "
                                + "Theatre Booking & Reservation System.")
                        .version("v1")
                        .contact(new Contact().name("Theatre Platform Team"))
                        .license(new License().name("Apache 2.0")))
                // Relative server URL: Swagger resolves "Try it out" against whatever
                // host/context the docs were loaded from, so it works locally and
                // behind the ALB context path (/booking-service) without hardcoding a host.
                .servers(List.of(
                        new Server().url("/").description("Current host")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token issued by the Identity Service. "
                                        + "Use the format: Bearer <token>")));
    }
}
