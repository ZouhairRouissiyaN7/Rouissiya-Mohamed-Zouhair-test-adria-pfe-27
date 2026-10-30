package com.adria.transaction_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "bank.transaction")
public record TransactionProperties(
        BigDecimal maxAmount) {
}
