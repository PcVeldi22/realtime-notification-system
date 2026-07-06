package com.pcveldi.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification.events")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic notificationDeadLetterTopic() {
        return TopicBuilder.name("notification.events.dlq")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        FixedBackOff backOff = new FixedBackOff(2000L, 3L);
        return new DefaultErrorHandler(backOff);
    }
}
