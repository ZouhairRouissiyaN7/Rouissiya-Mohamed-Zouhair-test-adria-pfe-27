package com.adria.transaction_service.mcp;

import com.adria.transaction_service.config.McpAuthentication;
import com.adria.transaction_service.dtos.*;
import com.adria.transaction_service.services.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionMcpTools {

    private final TransactionService service;
    private final McpAuthentication authentication;

    @Tool(description = "Enregistre une transaction qui débite un compte bancaire. Le montant doit être strictement positif et respecter le plafond autorisé, le compte doit être actif et la devise identique à celle du compte")
    public TransactionDTO createTransaction(
            @ToolParam(description = "Identifiant UUID du compte à débiter") String accountId,
            @ToolParam(description = "Montant de la transaction, strictement positif") double amount,
            @ToolParam(description = "Devise ISO sur 3 lettres, identique à celle du compte, par exemple EUR") String currency,
            @ToolParam(description = "Catégorie du commerçant, par exemple GROCERY ou TRAVEL") String merchantCategory,
            @ToolParam(description = "Code pays ISO sur 2 lettres, par exemple MA ou FR") String country,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        return authentication.call(bearerToken, auth -> service.create(
                new CreateTransactionRequest(
                        UUID.fromString(accountId),
                        BigDecimal.valueOf(amount),
                        currency,
                        merchantCategory,
                        country)));
    }

    @Tool(description = "Liste les 20 dernières transactions d'un compte bancaire, les plus récentes en premier")
    public List<TransactionDTO> listTransactions(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Jeton d'authentification de l'utilisateur, fourni automatiquement par la plateforme") String bearerToken) {
        return authentication.call(bearerToken, auth -> service.listByAccount(
                UUID.fromString(accountId),
                PageRequest.of(0, 20, Sort.by("createdAt").descending()))
                .getContent());
    }

}
