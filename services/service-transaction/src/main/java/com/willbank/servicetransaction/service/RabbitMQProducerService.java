package com.willbank.servicetransaction.service;

import com.willbank.servicetransaction.config.RabbitMQConfig;
import com.willbank.servicetransaction.dto.TransactionEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducerService {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendTransactionEvent(TransactionEventDTO event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_EXCHANGE,
                RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                event
            );
            log.info("Transaction event sent to RabbitMQ: {}", event);
        } catch (Exception e) {
            log.error("Error sending transaction event to RabbitMQ", e);
        }
    }
}
