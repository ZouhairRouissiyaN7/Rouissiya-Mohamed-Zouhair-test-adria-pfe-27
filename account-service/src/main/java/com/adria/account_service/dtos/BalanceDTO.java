package com.adria.account_service.dtos;

import java.util.UUID;

public record BalanceDTO(
        UUID accountId,
        String currency,
        double balance) {
}
