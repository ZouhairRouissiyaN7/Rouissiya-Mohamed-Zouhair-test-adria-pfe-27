package com.adria.transaction_service.config;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

        public static final String TRANSACTION_CREATED_TOPIC = "bank.transactions.created";

        @Bean
        NewTopic transactionCreatedTopic() {
                return TopicBuilder.name(TRANSACTION_CREATED_TOPIC)
                                .partitions(3)
                                .replicas(1)
                                .build();
        }
}
