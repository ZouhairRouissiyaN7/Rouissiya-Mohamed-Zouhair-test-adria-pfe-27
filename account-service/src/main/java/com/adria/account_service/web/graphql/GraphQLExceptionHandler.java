package com.adria.account_service.web.graphql;

import com.adria.account_service.exceptions.*;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handleAccountNotFound(
            AccountNotFoundException ex,
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
}
