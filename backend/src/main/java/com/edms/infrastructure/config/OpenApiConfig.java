package com.edms.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("EDMS API Documentation")
                        .version("1.0.0")
                        .description("REST API Contract for Enterprise Document Management System (EDMS) Backend.\n\n" +
                                "### 🔐 Mock Account Credentials for Testing:\n" +
                                "- **Owner Account**: `owner@edms.vn` / `Password123!`\n" +
                                "- **Editor Account**: `editor@edms.vn` / `Password123!`\n" +
                                "- **Viewer Account**: `viewer@edms.vn` / `Password123!`\n" +
                                "- **Manager Account**: `manager@edms.vn` / `Password123!`\n" +
                                "- **Admin Account**: `admin@edms.vn` / `Password123!`")
                        .contact(new Contact().name("EDMS Dev Team").email("support@edms.vn")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Token obtained from /auth/login")));
    }
}
