package com.example.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${rabbitmq.queue.transactions}")
    private String transactionQueue;
    
    @Value("${rabbitmq.queue.notifications}")
    private String notificationQueue;
    
    @Value("${rabbitmq.exchange.transactions}")
    private String transactionExchange;
    
    @Value("${rabbitmq.routing-key.transactions}")
    private String transactionRoutingKey;
    
    @Bean
    public Queue transactionQueue() {
        return new Queue(transactionQueue, true);
    }
    
    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }
    
    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }
    
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
            .bind(transactionQueue())
            .to(transactionExchange())
            .with(transactionRoutingKey);
    }
    
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
}
