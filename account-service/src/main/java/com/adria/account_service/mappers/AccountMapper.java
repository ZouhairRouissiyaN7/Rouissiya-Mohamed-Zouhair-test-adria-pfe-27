package com.adria.account_service.mappers;

import com.adria.account_service.dtos.*;
import com.adria.account_service.entities.Account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDTO toDTO(Account account);

    @Mapping(target = "accountId", source = "id")
    BalanceDTO toBalanceDTO(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Account toEntity(CreateAccountRequest request);

    default List<AccountDTO> toDTOList(
            List<Account> accounts) {
        return accounts.stream()
                .map(this::toDTO)
                .toList();
    }
}
