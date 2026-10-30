package com.adria.transaction_service.mappers;

import com.adria.transaction_service.dtos.*;
import com.adria.transaction_service.entities.Transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDTO toDTO(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    default List<TransactionDTO> toDTOList(
            List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toDTO)
                .toList();
    }
}
