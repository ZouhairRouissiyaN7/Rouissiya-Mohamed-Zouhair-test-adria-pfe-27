package com.adria.transaction_service.services.Impl;

import com.adria.transaction_service.clients.AccountClient;
import com.adria.transaction_service.config.TransactionProperties;
import com.adria.transaction_service.dtos.*;
import com.adria.transaction_service.entities.Transaction;
import com.adria.transaction_service.exceptions.*;
import com.adria.transaction_service.mappers.TransactionMapper;
import com.adria.transaction_service.repositories.TransactionRepository;
import com.adria.transaction_service.services.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

        private final TransactionRepository repository;
        private final TransactionMapper mapper;
        private final AccountClient accountClient;
        private final TransactionProperties properties;

        @Override
        public TransactionDTO create(CreateTransactionRequest request) {
                validAmount(request.amount());

                String authorization = bearerToken();

                AccountClient.AccountResponse account = accountClient.getAccount(
                                request.accountId(),
                                authorization);

                requireActive(account);
                requireSameCurrency(account, request.currency());
                ensureSufficientFunds(account, request.amount());

                Transaction transaction = mapper.toEntity(request);

                transaction.setCurrency(request.currency().toUpperCase());
                transaction.setCountry(request.country().toUpperCase());

                transaction = repository.saveAndFlush(transaction);

                accountClient.applyBalanceChange(
                                request.accountId(),
                                new AccountClient.BalanceChangeRequest(
                                                request.amount().negate()),
                                authorization);

                return mapper.toDTO(transaction);
        }

        @Override
        @Transactional(readOnly = true)
        public TransactionDTO get(UUID id) {
                Transaction transaction = findTransaction(id);

                accountClient.getAccount(
                                transaction.getAccountId(),
                                bearerToken());

                return mapper.toDTO(transaction);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TransactionDTO> listByAccount(
                        UUID accountId,
                        Pageable pageable) {
                accountClient.getAccount(accountId, bearerToken());

                return repository.findByAccountId(accountId, pageable)
                                .map(mapper::toDTO);
        }

        private Transaction findTransaction(UUID id) {
                return repository.findById(id)
                                .orElseThrow(() -> new TransactionNotFoundException(id));
        }

        private void validAmount(BigDecimal amount) {
                if (amount == null || amount.signum() <= 0) {
                        throw new BankingException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Le montant doit être strictement positif");
                }

                if (amount.compareTo(properties.maxAmount()) > 0) {
                        throw new BankingException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Le montant dépasse le plafond autorisé : " + properties.maxAmount());
                }
        }

        private void requireActive(AccountClient.AccountResponse account) {
                if (!"ACTIVE".equals(account.status())) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Le compte n'est pas actif : " + account.id());
                }
        }

        private void requireSameCurrency(
                        AccountClient.AccountResponse account,
                        String currency) {
                if (!account.currency().equalsIgnoreCase(currency)) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "La devise de la transaction ne correspond pas à celle du compte : "
                                                        + account.currency());
                }
        }

        private void ensureSufficientFunds(
                        AccountClient.AccountResponse account,
                        BigDecimal amount) {
                if (BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Solde insuffisant pour le compte : " + account.id());
                }
        }

        private String bearerToken() {
                var authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authentification JWT valide requise");
                }

                return "Bearer " + jwtAuth.getToken().getTokenValue();
        }
}
