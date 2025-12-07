package com.willbank.servicetransaction.listener;

import com.willbank.servicetransaction.config.RabbitMQConfig;
import com.willbank.servicetransaction.dto.TransactionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener pour consommer les événements de transaction depuis RabbitMQ
 * Ce composant est optionnel et sert à démontrer la consommation des événements
 */
@Component
@Slf4j
public class TransactionEventListener {
    
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void handleTransactionEvent(TransactionEventDTO event) {
        log.info("========================================");
        log.info("Événement de transaction reçu:");
        log.info("  Transaction ID: {}", event.getTransactionId());
        log.info("  Compte source: {}", event.getAccountIdSource());
        log.info("  Compte destination: {}", event.getAccountIdDestination());
        log.info("  Montant: {}", event.getAmount());
        log.info("  Type: {}", event.getType());
        log.info("  Statut: {}", event.getStatus());
        log.info("  Timestamp: {}", event.getTimestamp());
        log.info("========================================");
        
        // Ici vous pouvez ajouter votre logique métier:
        // - Mettre à jour les soldes des comptes
        // - Envoyer des notifications
        // - Déclencher d'autres processus
        // - Logger dans un système d'audit
        // etc.
    }
}
