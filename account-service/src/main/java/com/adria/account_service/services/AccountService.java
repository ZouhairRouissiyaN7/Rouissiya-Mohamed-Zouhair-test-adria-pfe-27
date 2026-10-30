package com.adria.account_service.services;

import com.adria.account_service.dtos.*;
import com.adria.account_service.enums.AccountStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

        AccountDTO create(CreateAccountRequest request);

        AccountDTO get(UUID id);

        BalanceDTO getBalance(UUID id);

        List<AccountDTO> list();

        List<AccountDTO> listByCustomerReference(String customerReference);

        AccountDTO updateStatus(
                        UUID id,
                        AccountStatus status);

        AccountDTO applyBalanceChange(
                        UUID id,
                        BigDecimal delta);

        TransferResultDTO transfer(TransferRequest request);
}
