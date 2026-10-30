package com.adria.account_service.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BalanceUpdateRequest(
        @NotNull(message = "La variation du solde est obligatoire") @Digits(integer = 17, fraction = 2, message = "Montant invalide") BigDecimal delta) {
}
