package com.adria.gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

@Configuration
public class GatewaySecurityConfig {

        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder(
                        @Value("${app.security.jwt.jwk-set-uri}") String jwks,
                        @Value("${app.security.jwt.issuer}") String issuer,
                        @Value("${app.security.jwt.audience}") String audience) {
                NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwks)
                                .jwsAlgorithm(
                                                org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                                .build();

                OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(
                                                new OAuth2Error(
                                                                "invalid_token",
                                                                "Audience du jeton JWT invalide",
                                                                null));

                decoder.setJwtValidator(
                                new DelegatingOAuth2TokenValidator<>(
                                                JwtValidators.createDefaultWithIssuer(issuer),
                                                audienceValidator));

                return decoder;
        }

        @Bean
        SecurityWebFilterChain securityWebFilterChain(
                        ServerHttpSecurity http) {

                JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();

                roles.setAuthoritiesClaimName("roles");
                roles.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();

                jwtConverter.setJwtGrantedAuthoritiesConverter(roles);

                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                                .authorizeExchange(auth -> auth

                                                .pathMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/docs/account/**",
                                                                "/docs/transaction/**")
                                                .permitAll()

                                                .pathMatchers(
                                                                HttpMethod.GET,
                                                                "/actuator/health")
                                                .permitAll()

                                                .pathMatchers(
                                                                "/api/accounts/graphiql/**",
                                                                "/api/transactions/graphiql/**")
                                                .permitAll()

                                                .pathMatchers(
                                                                "/api/accounts/**",
                                                                "/api/transactions/**")
                                                .authenticated()
                                                .pathMatchers(
                                                                HttpMethod.POST,
                                                                "/api/chat")
                                                .authenticated()
                                                .anyExchange().denyAll())
                                .oauth2ResourceServer(oauth -> oauth
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                new ReactiveJwtAuthenticationConverterAdapter(
                                                                                                jwtConverter))))
                                .build();
        }
}
