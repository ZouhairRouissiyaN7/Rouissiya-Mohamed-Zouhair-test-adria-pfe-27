package com.adria.account_service.mcp;

import com.adria.account_service.config.AccountAccess;
import com.adria.account_service.config.McpAuthentication;
import com.adria.account_service.dtos.*;
import com.adria.account_service.services.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService service;
    private final AccountAccess access;
    private final McpAuthentication authentication;

    @Tool(description = "Consulte un compte bancaire à partir de son identifiant UUID : statut, devise et solde")
    public AccountDTO getAccount(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        UUID id = UUID.fromString(accountId);

        return authentication.call(bearerToken, auth -> {
            requireOwnsAccount(id, auth);

            return service.get(id);
        });
    }

    @Tool(description = "Consulte le solde courant d'un compte bancaire à partir de son identifiant UUID")
    public BalanceDTO getBalance(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        UUID id = UUID.fromString(accountId);

        return authentication.call(bearerToken, auth -> {
            requireOwnsAccount(id, auth);

            return service.getBalance(id);
        });
    }

    @Tool(description = "Liste tous les comptes bancaires de la banque, réservé aux administrateurs")
    public List<AccountDTO> listAccounts(
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        return authentication.call(bearerToken, auth -> {
            if (!access.isAdmin(auth)) {
                throw new AccessDeniedException("Réservé aux administrateurs");
            }

            return service.list();
        });
    }

    @Tool(description = "Liste les comptes bancaires d'un client à partir de sa référence client")
    public List<AccountDTO> listCustomerAccounts(
            @ToolParam(description = "Référence du client, par exemple customer1") String customerReference,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        return authentication.call(bearerToken, auth -> {
            if (!access.ownsCustomer(customerReference, auth)) {
                throw new AccessDeniedException("Accès refusé à ce client");
            }

            return service.listByCustomerReference(customerReference);
        });
    }

    @Tool(description = "Effectue un virement d'un compte vers un autre compte de même devise")
    public TransferResultDTO transfer(
            @ToolParam(description = "Identifiant UUID du compte à débiter") String sourceAccountId,
            @ToolParam(description = "Identifiant UUID du compte à créditer") String destinationAccountId,
            @ToolParam(description = "Montant du virement, strictement positif") double amount,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        UUID source = UUID.fromString(sourceAccountId);
        UUID destination = UUID.fromString(destinationAccountId);

        return authentication.call(bearerToken, auth -> {
            requireOwnsAccount(source, auth);

            return service.transfer(new TransferRequest(
                    source,
                    destination,
                    BigDecimal.valueOf(amount)));
        });
    }

    private void requireOwnsAccount(
            UUID accountId,
            Authentication auth) {
        if (!access.ownsAccount(accountId, auth)) {
            throw new AccessDeniedException("Accès refusé à ce compte");
        }
    }

}
