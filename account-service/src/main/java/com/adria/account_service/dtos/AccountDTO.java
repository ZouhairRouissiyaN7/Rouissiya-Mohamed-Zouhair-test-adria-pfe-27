package com.adria.account_service.dtos;

import com.adria.account_service.enums.AccountStatus;

import java.util.UUID;

public record AccountDTO(
        UUID id,
        String customerReference,
        AccountStatus status,
        String currency,
        double balance) {
}
