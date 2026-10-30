package com.adria.transaction_service.web.graphql;

import com.adria.transaction_service.dtos.*;
import com.adria.transaction_service.services.TransactionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService service;

    @QueryMapping
    public TransactionDTO transaction(
            @Argument UUID id) {
        return service.get(id);
    }

    @QueryMapping
    public List<TransactionDTO> transactionsByAccount(
            @Argument UUID accountId,
            @Argument int page,
            @Argument int size) {
        return service.listByAccount(
                accountId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .getContent();
    }

    @MutationMapping
    public TransactionDTO createTransaction(
            @Argument("input") @Valid CreateTransactionRequest request) {
        return service.create(request);
    }

}
