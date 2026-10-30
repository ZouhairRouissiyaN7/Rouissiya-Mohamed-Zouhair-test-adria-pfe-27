package com.adria.account_service.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResultDTO(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        double sourceBalance,
        double destinationBalance) {
}
