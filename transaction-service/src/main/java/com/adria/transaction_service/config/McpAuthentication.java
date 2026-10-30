package com.adria.transaction_service.config;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class McpAuthentication {

    private final JwtDecoder decoder;
    private final JwtAuthenticationConverter converter;

    public <T> T call(
            String bearerToken,
            Function<Authentication, T> action) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new AccessDeniedException("Jeton d'authentification manquant");
        }

        String token = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;

        Authentication authentication = converter.convert(decoder.decode(token));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        try {
            return action.apply(authentication);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
