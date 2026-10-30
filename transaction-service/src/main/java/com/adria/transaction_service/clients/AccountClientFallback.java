
package com.adria.transaction_service.clients;

import com.adria.transaction_service.exceptions.BankingException;

import feign.FeignException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
public class AccountClientFallback implements FallbackFactory<AccountClient> {

    @Override
    public AccountClient create(Throwable cause) {

        return new AccountClient() {

            @Override
            public AccountResponse getAccount(
                    UUID id,
                    String authorization) {
                throw failure(cause, id);
            }

            @Override
            public AccountResponse applyBalanceChange(
                    UUID id,
                    BalanceChangeRequest request,
                    String authorization) {
                throw failure(cause, id);
            }
        };

    }

    private RuntimeException failure(
            Throwable cause,
            UUID id) {

        if (cause instanceof FeignException.NotFound) {
            return new BankingException(
                    HttpStatus.NOT_FOUND,
                    "Compte introuvable : " + id);
        }

        if (cause instanceof FeignException.Unauthorized) {
            return new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentification refusée par le service de comptes",
                    cause);
        }

        if (cause instanceof FeignException.Forbidden) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Accès refusé à ce compte",
                    cause);
        }

        if (cause instanceof FeignException.Conflict) {
            return new BankingException(
                    HttpStatus.CONFLICT,
                    "Opération refusée par le service de comptes : compte inactif ou solde insuffisant");
        }

        if (cause instanceof FeignException.BadRequest
                || cause instanceof FeignException.UnprocessableEntity) {
            return new BankingException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Requête refusée par le service de comptes");
        }

        log.warn("Service de comptes indisponible : {}", cause.toString());

        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Le service de comptes est temporairement indisponible",
                cause);
    }
}
