package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();
        hideSchema(components, "HttpMethod");
        hideSchema(components, "HttpRange");
        hideSchema(components, "HttpStatusCode");
        hideSchema(components, "MediaType");
        hideSchema(components, "ProblemDetail");

        components.addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Введите JWT токен в формате: Bearer <token>")
        );

        return new OpenAPI()
                .info(new Info()
                        .title("Bank_REST API")
                        .version("1.0.0")
                        .description("API для управления банковскими картами и пользователями")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Локальный сервер")
                ))
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .externalDocs(new ExternalDocumentation()
                        .description("Архитектура системы")
                        .url("/api-docs/architecture"))
                .externalDocs(new ExternalDocumentation()
                        .description("Пользовательские сценарии")
                        .url("/api-docs/user-scenarios"));
    }

    private void hideSchema(Components components, String schemaName) {
        Schema<?> schema = new Schema<>().type("object");
        schema.addExtension("x-hidden", true);
        components.addSchemas(schemaName, schema);
    }

}
