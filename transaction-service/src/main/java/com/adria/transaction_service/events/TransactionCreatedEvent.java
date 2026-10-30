package com.adria.transaction_service.events;

import com.adria.transaction_service.dtos.TransactionDTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt) {

    public static TransactionCreatedEvent from(TransactionDTO transaction) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                "TransactionCreated",
                transaction.id(),
                transaction.accountId(),
                transaction.amount().setScale(2, RoundingMode.HALF_UP),
                transaction.currency(),
                transaction.createdAt());
    }
}
