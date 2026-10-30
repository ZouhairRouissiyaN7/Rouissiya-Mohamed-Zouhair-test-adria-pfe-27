package com.adria.account_service.repositories;

import com.adria.account_service.entities.Account;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    List<Account> findByCustomerReference(String customerReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(
            @Param("id") UUID id);
}
