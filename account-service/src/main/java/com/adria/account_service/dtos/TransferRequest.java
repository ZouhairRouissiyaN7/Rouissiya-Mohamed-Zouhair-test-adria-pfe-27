package com.adria.account_service.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "Le compte source est obligatoire") UUID sourceAccountId,

        @NotNull(message = "Le compte destinataire est obligatoire") UUID destinationAccountId,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.00", inclusive = false, message = "Le montant doit être strictement positif") @Digits(integer = 17, fraction = 2, message = "Montant invalide") BigDecimal amount) {
}
