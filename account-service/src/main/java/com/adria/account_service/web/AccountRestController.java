package com.adria.account_service.web;

import com.adria.account_service.dtos.*;
import com.adria.account_service.services.AccountService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Comptes", description = "Gestion des comptes bancaires")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final AccountService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO create(
            @Valid @RequestBody CreateAccountRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AccountDTO> list() {
        return service.list();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me")
    public List<AccountDTO> listMine(
            @AuthenticationPrincipal Jwt jwt) {
        return service.listByCustomerReference(
                jwt.getClaimAsString("preferred_username"));
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @GetMapping("/{id}")
    public AccountDTO get(
            @PathVariable UUID id) {
        return service.get(id);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @GetMapping("/{id}/balance")
    public BalanceDTO getBalance(
            @PathVariable UUID id) {
        return service.getBalance(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public AccountDTO updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    @PreAuthorize("hasRole('ADMIN') or (@accountAccess.ownsAccount(#id, authentication) and #request.delta().signum() < 0)")
    @PutMapping("/{id}/balance")
    public AccountDTO applyBalanceChange(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return service.applyBalanceChange(id, request.delta());
    }

    @PreAuthorize("@accountAccess.ownsAccount(#request.sourceAccountId(), authentication)")
    @PostMapping("/transfer")
    public TransferResultDTO transfer(
            @Valid @RequestBody TransferRequest request) {
        return service.transfer(request);
    }

}
