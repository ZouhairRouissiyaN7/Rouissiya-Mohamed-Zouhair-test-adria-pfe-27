package com.adria.account_service.web.graphql;

import com.adria.account_service.dtos.*;
import com.adria.account_service.enums.AccountStatus;
import com.adria.account_service.services.AccountService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService service;

    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public List<AccountDTO> accounts() {
        return service.list();
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @QueryMapping
    public AccountDTO account(
            @Argument UUID id) {
        return service.get(id);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @QueryMapping
    public BalanceDTO accountBalance(
            @Argument UUID id) {
        return service.getBalance(id);
    }

    @PreAuthorize("@accountAccess.ownsCustomer(#customerReference, authentication)")
    @QueryMapping
    public List<AccountDTO> accountsByCustomer(
            @Argument String customerReference) {
        return service.listByCustomerReference(customerReference);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @MutationMapping
    public AccountDTO createAccount(
            @Argument("input") @Valid CreateAccountRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @MutationMapping
    public AccountDTO updateAccountStatus(
            @Argument UUID id,
            @Argument AccountStatus status) {
        return service.updateStatus(id, status);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#request.sourceAccountId(), authentication)")
    @MutationMapping
    public TransferResultDTO transfer(
            @Argument("input") @Valid TransferRequest request) {
        return service.transfer(request);
    }

}
