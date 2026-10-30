
package com.adria.transaction_service.web;

import com.adria.transaction_service.exceptions.*;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(TransactionNotFoundException.class)
        public ProblemDetail handleTransactionNotFound(
                        TransactionNotFoundException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleValidation(
                        MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + " : "
                                                + error.getDefaultMessage())
                                .findFirst()
                                .orElse("Requête invalide");

                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                message);
        }

        @ExceptionHandler(BankingException.class)
        public ProblemDetail handleBankingException(
                        BankingException ex) {
                return ProblemDetail.forStatusAndDetail(
                                ex.getStatus(),
                                ex.getMessage());
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ProblemDetail handleResponseStatus(
                        ResponseStatusException ex) {
                return ProblemDetail.forStatusAndDetail(
                                ex.getStatusCode(),
                                ex.getReason());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ProblemDetail handleDataConflict(
                        DataIntegrityViolationException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                "L'opération est en conflit avec les données existantes");
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ProblemDetail handleInvalidParameter(
                        MethodArgumentTypeMismatchException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "Paramètre de requête invalide : " + ex.getName());
        }
}
