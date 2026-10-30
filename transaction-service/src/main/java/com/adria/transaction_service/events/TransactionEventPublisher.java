package com.adria.transaction_service.events;

import com.adria.transaction_service.config.KafkaConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate
                .send(
                        KafkaConfig.TRANSACTION_CREATED_TOPIC,
                        event.accountId().toString(),
                        event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Publication Kafka impossible pour la transaction {}", event.transactionId(), failure);
                    }
                });
    }
}
