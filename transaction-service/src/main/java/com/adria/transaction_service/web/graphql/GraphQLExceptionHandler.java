package com.adria.transaction_service.web.graphql;

import com.adria.transaction_service.exceptions.*;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handleTransactionNotFound(
            TransactionNotFoundException ex,
            DataFetchingEnvironment environment) {
        return GraphQLError.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleBankingException(
            BankingException ex,
            DataFetchingEnvironment environment) {
        return GraphQLError.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleResponseStatus(
            ResponseStatusException ex,
            DataFetchingEnvironment environment) {
        ErrorType type = switch (ex.getStatusCode().value()) {
            case 401 -> ErrorType.UNAUTHORIZED;
            case 403 -> ErrorType.FORBIDDEN;
            default -> ErrorType.INTERNAL_ERROR;
        };

        return GraphQLError.newError()
                .errorType(type)
                .message(ex.getReason())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }
}
