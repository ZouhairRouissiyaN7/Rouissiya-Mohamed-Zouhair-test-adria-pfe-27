package com.adria.account_service.dtos;

import jakarta.validation.constraints.*;

public record CreateAccountRequest(
        @NotBlank(message = "La référence client est obligatoire") String customerReference,

        @NotBlank(message = "La devise est obligatoire") @Size(min = 3, max = 3, message = "La devise doit comporter 3 caractères") String currency,

        @PositiveOrZero(message = "Le solde initial ne peut pas être négatif") double balance) {
}
