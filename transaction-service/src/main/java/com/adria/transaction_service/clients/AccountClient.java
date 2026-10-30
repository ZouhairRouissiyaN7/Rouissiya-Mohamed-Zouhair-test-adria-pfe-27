package com.adria.transaction_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "account-service", fallbackFactory = AccountClientFallback.class)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountResponse getAccount(
            @PathVariable("id") UUID id,
            @RequestHeader("Authorization") String authorization);

    @PutMapping("/api/accounts/{id}/balance")
    AccountResponse applyBalanceChange(
            @PathVariable("id") UUID id,
            @RequestBody BalanceChangeRequest request,
            @RequestHeader("Authorization") String authorization);

    record AccountResponse(
            UUID id,
            String customerReference,
            String status,
            String currency,
            double balance) {
    }

    record BalanceChangeRequest(
            BigDecimal delta) {
    }
}
