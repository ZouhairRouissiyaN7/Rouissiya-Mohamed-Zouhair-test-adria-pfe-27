package com.adria.account_service.mcp;

import com.adria.account_service.dtos.*;
import com.adria.account_service.services.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService service;

    @Tool(description = "Consulte un compte bancaire à partir de son identifiant UUID : statut, devise et solde")
    public AccountDTO getAccount(
            @ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return service.get(UUID.fromString(accountId));
    }

    @Tool(description = "Consulte le solde courant d'un compte bancaire à partir de son identifiant UUID")
    public BalanceDTO getBalance(
            @ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return service.getBalance(UUID.fromString(accountId));
    }

    @Tool(description = "Liste tous les comptes bancaires de la banque")
    public List<AccountDTO> listAccounts() {
        return service.list();
    }

    @Tool(description = "Liste les comptes bancaires d'un client à partir de sa référence client")
    public List<AccountDTO> listCustomerAccounts(
            @ToolParam(description = "Référence du client, par exemple customer1") String customerReference) {
        return service.listByCustomerReference(customerReference);
    }

    @Tool(description = "Effectue un virement d'un compte vers un autre compte de même devise")
    public TransferResultDTO transfer(
            @ToolParam(description = "Identifiant UUID du compte à débiter") String sourceAccountId,
            @ToolParam(description = "Identifiant UUID du compte à créditer") String destinationAccountId,
            @ToolParam(description = "Montant du virement, strictement positif") double amount) {
        return service.transfer(new TransferRequest(
                UUID.fromString(sourceAccountId),
                UUID.fromString(destinationAccountId),
                BigDecimal.valueOf(amount)));
    }

}
