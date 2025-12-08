package com.willbank.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Transaction Service Application
 * Main entry point for WillBank's transaction management microservice
 * 
 * Responsibilities:
 * - Process deposits, withdrawals, and transfers
 * - Communicate with account-service for balance updates
 * - Publish transaction events to RabbitMQ
 * - Maintain transaction audit trail
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
