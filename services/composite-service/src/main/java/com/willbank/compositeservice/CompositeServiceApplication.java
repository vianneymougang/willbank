package com.willbank.compositeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Composite Service Application
 * Main entry point for WillBank's composite microservice
 * 
 * Responsibilities:
 * - Aggregate data from user-service, account-service, and transaction-service
 * - Provide unified business-oriented endpoints for front-end applications
 * - Implement circuit breaker and retry patterns for resilience
 */
@SpringBootApplication(scanBasePackages = { "com.willbank.composite", "com.willbank.compositeservice" })
@EnableFeignClients(basePackages = "com.willbank.composite.client")
@EnableDiscoveryClient
public class CompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompositeServiceApplication.class, args);
	}

}
