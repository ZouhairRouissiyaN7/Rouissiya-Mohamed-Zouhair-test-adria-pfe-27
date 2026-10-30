package com.adria.chat_service.dtos;

import jakarta.validation.constraints.*;

public record ChatRequest(
        @NotBlank(message = "Le message est obligatoire") @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères") String message) {
}
