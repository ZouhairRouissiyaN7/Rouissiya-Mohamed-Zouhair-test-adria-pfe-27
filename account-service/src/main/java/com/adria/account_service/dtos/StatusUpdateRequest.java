package com.adria.account_service.dtos;

import com.adria.account_service.enums.AccountStatus;

import jakarta.validation.constraints.*;

public record StatusUpdateRequest(
        @NotNull(message = "Le statut est obligatoire") AccountStatus status) {
}
