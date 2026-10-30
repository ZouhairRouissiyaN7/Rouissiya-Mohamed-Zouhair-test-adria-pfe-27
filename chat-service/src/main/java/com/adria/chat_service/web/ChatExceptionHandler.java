package com.adria.chat_service.web;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ChatExceptionHandler {

    @ExceptionHandler({
            org.springframework.ai.retry.NonTransientAiException.class,
            org.springframework.ai.retry.TransientAiException.class,
            org.springframework.web.client.ResourceAccessException.class
    })
    public ProblemDetail handleAiError(Exception ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Le modèle d'IA local est temporairement indisponible.");
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
}
