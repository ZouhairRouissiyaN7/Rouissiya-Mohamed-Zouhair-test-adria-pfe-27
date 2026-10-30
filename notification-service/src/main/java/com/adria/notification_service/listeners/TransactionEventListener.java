package com.adria.notification_service.listeners;

import com.adria.notification_service.events.TransactionCreatedEvent;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventListener {

    @KafkaListener(topics = "bank.transactions.created")
    public void onTransactionCreated(
            ConsumerRecord<String, TransactionCreatedEvent> record) {
        TransactionCreatedEvent event = record.value();

        log.info(
                "Événement reçu : topic={} partition={} offset={} clé={} eventId={} type={} transactionId={} accountId={} montant={} {} occurredAt={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                event.eventId(),
                event.eventType(),
                event.transactionId(),
                event.accountId(),
                event.amount(),
                event.currency(),
                event.occurredAt());
    }
}
