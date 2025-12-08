package com.willbank.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for User Service
 * Defines exchanges, queues, bindings, and message converter for event
 * publishing
 */
@Configuration
public class RabbitMQConfig {

    // ============================================
    // Exchange Names
    // ============================================
    public static final String CUSTOMER_EXCHANGE = "willbank.customer.exchange";
    public static final String TRANSACTION_EXCHANGE = "willbank.transaction.exchange";

    // ============================================
    // Queue Names - Customer Events
    // ============================================
    public static final String CUSTOMER_CREATED_QUEUE = "willbank.customer.created";
    public static final String KYC_STATUS_CHANGED_QUEUE = "willbank.customer.kyc_status_changed";

    // ============================================
    // Queue Names - Transaction Events (Prepared for future)
    // ============================================
    public static final String TRANSACTION_COMPLETED_QUEUE = "willbank.transaction.completed";
    public static final String LARGE_TRANSACTION_ALERT_QUEUE = "willbank.transaction.large_alert";
    public static final String INSUFFICIENT_FUNDS_QUEUE = "willbank.transaction.insufficient_funds";

    // ============================================
    // Routing Keys - Customer
    // ============================================
    public static final String CUSTOMER_CREATED_ROUTING_KEY = "customer.created";
    public static final String KYC_STATUS_CHANGED_ROUTING_KEY = "customer.kyc.status.changed";

    // ============================================
    // Routing Keys - Transaction (Prepared for future)
    // ============================================
    public static final String TRANSACTION_COMPLETED_ROUTING_KEY = "transaction.completed";
    public static final String LARGE_TRANSACTION_ALERT_ROUTING_KEY = "transaction.large.alert";
    public static final String INSUFFICIENT_FUNDS_ROUTING_KEY = "transaction.insufficient.funds";

    // ============================================
    // Dead Letter Exchange & Queue
    // ============================================
    public static final String DLX_EXCHANGE = "willbank.dlx";
    public static final String DLQ_QUEUE = "willbank.dlq";

    // ============================================
    // Message Converter Configuration (JSON)
    // ============================================

    /**
     * Configure Jackson2 JSON converter for RabbitMQ messages
     * This allows us to send POJOs without implementing Serializable
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON message converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ============================================
    // Customer Exchange & Queues
    // ============================================

    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange(CUSTOMER_EXCHANGE);
    }

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
    public Binding customerCreatedBinding() {
        return BindingBuilder
                .bind(customerCreatedQueue())
                .to(customerExchange())
                .with(CUSTOMER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding kycStatusChangedBinding() {
        return BindingBuilder
                .bind(kycStatusChangedQueue())
                .to(customerExchange())
                .with(KYC_STATUS_CHANGED_ROUTING_KEY);
    }

    // ============================================
    // Transaction Exchange & Queues (Infrastructure only, producers TBD)
    // ============================================

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
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

    @Bean
    public Queue insufficientFundsQueue() {
        return QueueBuilder.durable(INSUFFICIENT_FUNDS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding transactionCompletedBinding() {
        return BindingBuilder
                .bind(transactionCompletedQueue())
                .to(transactionExchange())
                .with(TRANSACTION_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding largeTransactionAlertBinding() {
        return BindingBuilder
                .bind(largeTransactionAlertQueue())
                .to(transactionExchange())
                .with(LARGE_TRANSACTION_ALERT_ROUTING_KEY);
    }

    @Bean
    public Binding insufficientFundsBinding() {
        return BindingBuilder
                .bind(insufficientFundsQueue())
                .to(transactionExchange())
                .with(INSUFFICIENT_FUNDS_ROUTING_KEY);
    }

    // ============================================
    // Dead Letter Queue Configuration
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
}
