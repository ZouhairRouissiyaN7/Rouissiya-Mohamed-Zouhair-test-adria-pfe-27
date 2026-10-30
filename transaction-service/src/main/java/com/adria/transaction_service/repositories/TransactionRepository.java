package com.adria.transaction_service.repositories;

import com.adria.transaction_service.entities.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccountId(
            UUID accountId,
            Pageable pageable);
}
