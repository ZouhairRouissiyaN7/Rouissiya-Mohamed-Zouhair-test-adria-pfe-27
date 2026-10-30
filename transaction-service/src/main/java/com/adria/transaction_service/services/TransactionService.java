package com.adria.transaction_service.services;

import com.adria.transaction_service.dtos.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

        TransactionDTO create(CreateTransactionRequest request);

        TransactionDTO get(UUID id);

        Page<TransactionDTO> listByAccount(
                        UUID accountId,
                        Pageable pageable);
}
