package com.adria.account_service.exceptions;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Compte introuvable : " + id);
    }
}
