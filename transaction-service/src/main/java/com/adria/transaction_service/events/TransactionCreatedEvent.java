package com.adria.transaction_service.events;

import com.adria.transaction_service.dtos.TransactionDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt) {

    public static TransactionCreatedEvent from(TransactionDTO transaction) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                "TransactionCreated",
                transaction.id(),
                transaction.accountId(),
                transaction.amount(),
                transaction.currency(),
                transaction.createdAt());
    }
}
