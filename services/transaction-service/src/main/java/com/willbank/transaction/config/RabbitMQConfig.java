package com.willbank.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Transaction Service
 * Publishes transaction events to notification-service
 */
@Configuration
public class RabbitMQConfig {

    // ============================================
    // Exchange Names
    // ============================================
    public static final String TRANSACTION_EXCHANGE = "willbank.transaction.exchange";

    // ============================================
    // Routing Keys
    // ============================================
    public static final String TRANSACTION_COMPLETED_ROUTING_KEY = "transaction.completed";
    public static final String LARGE_TRANSACTION_ALERT_ROUTING_KEY = "transaction.large.alert";

    // ============================================
    // Message Converter Configuration (JSON)
    // ============================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ============================================
    // Transaction Exchange
    // ============================================

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    // Note: Queues are declared in notification-service
    // We only declare the exchange here for publishing
}
