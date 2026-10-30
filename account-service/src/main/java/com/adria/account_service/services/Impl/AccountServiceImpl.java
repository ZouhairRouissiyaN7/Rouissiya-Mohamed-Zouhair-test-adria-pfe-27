package com.adria.account_service.services.Impl;

import com.adria.account_service.dtos.*;
import com.adria.account_service.entities.Account;
import com.adria.account_service.enums.AccountStatus;
import com.adria.account_service.exceptions.*;
import com.adria.account_service.mappers.AccountMapper;
import com.adria.account_service.repositories.AccountRepository;
import com.adria.account_service.services.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

        private final AccountRepository repository;
        private final AccountMapper mapper;

        @Override
        public AccountDTO create(CreateAccountRequest request) {
                Account account = mapper.toEntity(request);

                account.setCurrency(request.currency().toUpperCase());

                return mapper.toDTO(repository.saveAndFlush(account));
        }

        @Override
        @Transactional(readOnly = true)
        public AccountDTO get(UUID id) {
                return mapper.toDTO(findAccount(id));
        }

        @Override
        @Transactional(readOnly = true)
        public BalanceDTO getBalance(UUID id) {
                return mapper.toBalanceDTO(findAccount(id));
        }

        @Override
        @Transactional(readOnly = true)
        public List<AccountDTO> list() {
                return mapper.toDTOList(repository.findAll());
        }

        @Override
        @Transactional(readOnly = true)
        public List<AccountDTO> listByCustomerReference(
                        String customerReference) {
                return mapper.toDTOList(
                                repository.findByCustomerReference(customerReference));
        }

        @Override
        public AccountDTO updateStatus(
                        UUID id,
                        AccountStatus status) {
                Account account = findAccountForUpdate(id);

                account.setStatus(status);

                return mapper.toDTO(repository.save(account));
        }

        @Override
        public AccountDTO applyBalanceChange(
                        UUID id,
                        BigDecimal delta) {
                Account account = findAccountForUpdate(id);

                requireActive(account);
                move(account, delta);

                return mapper.toDTO(repository.save(account));
        }

        @Override
        public TransferResultDTO transfer(TransferRequest request) {
                validAmount(request.amount());
                requireDistinct(
                                request.sourceAccountId(),
                                request.destinationAccountId());

                Account source;
                Account destination;

                if (request.sourceAccountId().compareTo(request.destinationAccountId()) < 0) {
                        source = findAccountForUpdate(request.sourceAccountId());
                        destination = findAccountForUpdate(request.destinationAccountId());
                } else {
                        destination = findAccountForUpdate(request.destinationAccountId());
                        source = findAccountForUpdate(request.sourceAccountId());
                }

                requireActive(source);
                requireActive(destination);
                requireSameCurrency(source, destination);

                move(source, request.amount().negate());
                move(destination, request.amount());

                repository.save(source);
                repository.save(destination);

                return new TransferResultDTO(
                                source.getId(),
                                destination.getId(),
                                request.amount(),
                                source.getBalance(),
                                destination.getBalance());
        }

        private Account findAccount(UUID id) {
                return repository.findById(id)
                                .orElseThrow(() -> new AccountNotFoundException(id));
        }

        private Account findAccountForUpdate(UUID id) {
                return repository.findByIdForUpdate(id)
                                .orElseThrow(() -> new AccountNotFoundException(id));
        }

        private void requireActive(Account account) {
                if (account.getStatus() != AccountStatus.ACTIVE) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Le compte n'est pas actif : " + account.getId());
                }
        }

        private void requireSameCurrency(
                        Account source,
                        Account destination) {
                if (!source.getCurrency().equals(destination.getCurrency())) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Les comptes n'ont pas la même devise");
                }
        }

        private void requireDistinct(
                        UUID sourceAccountId,
                        UUID destinationAccountId) {
                if (sourceAccountId.equals(destinationAccountId)) {
                        throw new BankingException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Les comptes source et destinataire doivent être différents");
                }
        }

        private void validAmount(BigDecimal amount) {
                if (amount == null || amount.signum() <= 0) {
                        throw new BankingException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Le montant doit être strictement positif");
                }
        }

        private void ensureSufficientFunds(
                        BigDecimal updatedBalance,
                        Account account) {
                if (updatedBalance.signum() < 0) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Solde insuffisant pour le compte : " + account.getId());
                }
        }

        private void move(
                        Account account,
                        BigDecimal delta) {
                BigDecimal updated = BigDecimal.valueOf(account.getBalance())
                                .add(delta)
                                .setScale(2, RoundingMode.HALF_UP);

                ensureSufficientFunds(updated, account);

                account.setBalance(updated.doubleValue());
        }
}
