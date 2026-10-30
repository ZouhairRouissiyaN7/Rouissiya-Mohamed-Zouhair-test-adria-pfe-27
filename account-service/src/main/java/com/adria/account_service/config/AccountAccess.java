package com.adria.account_service.config;

import com.adria.account_service.repositories.AccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("accountAccess")
public class AccountAccess {

    private final AccountRepository repository;

    public AccountAccess(AccountRepository repository) {
        this.repository = repository;
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(
                        authority -> authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }

    public boolean ownsCustomer(
            String customerReference,
            Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String authenticatedReference = customerReference(authentication);

        return authenticatedReference != null
                && authenticatedReference.equals(customerReference);
    }

    public boolean ownsAccount(
            UUID accountId,
            Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String authenticatedReference = customerReference(authentication);

        if (authenticatedReference == null) {
            return false;
        }

        return repository.findById(accountId)
                .map(account -> authenticatedReference.equals(account.getCustomerReference()))
                .orElse(false);
    }

    private String customerReference(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return null;
        }

        if (jwt.getAuthorities().stream().noneMatch(
                authority -> authority.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            return null;
        }

        return jwt.getToken().getClaimAsString("preferred_username");
    }
}
