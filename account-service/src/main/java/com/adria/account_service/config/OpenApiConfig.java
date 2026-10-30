package com.adria.account_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI accountOpenAPI() {
                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes(
                                                                "bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(
                                                new SecurityRequirement().addList("bearerAuth"))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8888")
                                                                .description("Passerelle API locale")))
                                .info(new Info()
                                                .title("Banque ADRIA - API du service de comptes")
                                                .version("1.0.0")
                                                .description("""
                                                                API de gestion des comptes bancaires :
                                                                consultation, solde, blocage et virements.
                                                                """)
                                                .contact(new Contact()
                                                                .name("Projet Banque ADRIA")));
        }
}
