package com.willbank.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Notification Service
 * Defines queues, exchanges, and JSON message converter for consuming events
 */
@Configuration
public class RabbitMQConfig {

    // ============================================
    // Exchange Names (should match producer config)
    // ============================================
    public static final String CUSTOMER_EXCHANGE = "willbank.customer.exchange";
    public static final String ACCOUNT_EXCHANGE = "willbank.account.exchange";
    public static final String TRANSACTION_EXCHANGE = "willbank.transaction.exchange";

    // ============================================
    // Queue Names
    // ============================================
    public static final String CUSTOMER_CREATED_QUEUE = "willbank.customer.created";
    public static final String KYC_STATUS_CHANGED_QUEUE = "willbank.customer.kyc_status_changed";
    public static final String ACCOUNT_CREATED_QUEUE = "willbank.account.created";
    public static final String TRANSACTION_COMPLETED_QUEUE = "willbank.transaction.completed";
    public static final String LARGE_TRANSACTION_ALERT_QUEUE = "willbank.transaction.large_alert";

    // ============================================
    // Dead Letter
    // ============================================
    public static final String DLX_EXCHANGE = "willbank.dlx";
    public static final String DLQ_QUEUE = "willbank.dlq";

    // ============================================
    // Message Converter Configuration (JSON)
    // ============================================

    /**
     * Configure Jackson2 JSON converter for RabbitMQ messages
     * This allows consumers to deserialize JSON payloads to POJOs
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitListenerContainerFactory with JSON message converter
     * This is used by @RabbitListener annotations
     * Only created when ConnectionFactory is available (not in tests)
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(ConnectionFactory.class)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }

    // ============================================
    // Exchanges
    // ============================================

    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange(CUSTOMER_EXCHANGE);
    }

    @Bean
    public TopicExchange accountExchange() {
        return new TopicExchange(ACCOUNT_EXCHANGE);
    }

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    // ============================================
    // Queues (already defined in user-service, but ensuring they exist)
    // ============================================

    @Bean
    public Queue customerCreatedQueue() {
        return QueueBuilder.durable(CUSTOMER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue kycStatusChangedQueue() {
        return QueueBuilder.durable(KYC_STATUS_CHANGED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue accountCreatedQueue() {
        return QueueBuilder.durable(ACCOUNT_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue transactionCompletedQueue() {
        return QueueBuilder.durable(TRANSACTION_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue largeTransactionAlertQueue() {
        return QueueBuilder.durable(LARGE_TRANSACTION_ALERT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    // ============================================
    // Dead Letter Queue
    // ============================================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlq");
    }

    // ============================================
    // Transaction Queue Bindings
    // ============================================

    @Bean
    public Binding transactionCompletedBinding() {
        return BindingBuilder
                .bind(transactionCompletedQueue())
                .to(transactionExchange())
                .with("transaction.completed");
    }

    @Bean
    public Binding largeTransactionAlertBinding() {
        return BindingBuilder
                .bind(largeTransactionAlertQueue())
                .to(transactionExchange())
                .with("transaction.large_alert");
    }
}
