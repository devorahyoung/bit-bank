package com.poalim.mybank.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {
    
    public static final String TRANSACTION_TOPIC = "bank-transactions";
    public static final String DEPOSIT_TOPIC = "bank-deposits";
    public static final String TRANSACTION_DLT = "bank-transactions-dlt";
    public static final String DEPOSIT_DLT = "bank-deposits-dlt";
    
    @Bean
    public NewTopic transactionTopic() {
        return TopicBuilder.name(TRANSACTION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic depositTopic() {
        return TopicBuilder.name(DEPOSIT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic transactionDeadLetterTopic() {
        return TopicBuilder.name(TRANSACTION_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic depositDeadLetterTopic() {
        return TopicBuilder.name(DEPOSIT_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}