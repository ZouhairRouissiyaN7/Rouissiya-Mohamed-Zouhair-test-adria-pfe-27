package com.adria.transaction_service.exceptions;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(UUID id) {
        super("Transaction introuvable : " + id);
    }
}
