package com.adria.transaction_service.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull(message = "Le compte est obligatoire") UUID accountId,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.00", inclusive = false, message = "Le montant doit être strictement positif") @Digits(integer = 17, fraction = 2, message = "Montant invalide") BigDecimal amount,

        @NotBlank(message = "La devise est obligatoire") @Size(min = 3, max = 3, message = "La devise doit comporter 3 lettres") String currency,

        @NotBlank(message = "La catégorie du commerçant est obligatoire") String merchantCategory,

        @NotBlank(message = "Le pays est obligatoire") @Pattern(regexp = "^[A-Za-z]{2}$", message = "Le pays doit être un code ISO sur 2 lettres") String country) {
}
