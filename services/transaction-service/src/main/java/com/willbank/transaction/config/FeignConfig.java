package com.willbank.transaction.config;

import feign.Logger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Feign Client Configuration with Resilience4j Circuit Breaker
 * Provides fault tolerance for inter-service communication
 */
@Configuration
public class FeignConfig {

    /**
     * Configure Feign logging level
     * FULL = Log headers, body, and metadata for both requests and responses
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Configure Resilience4j Circuit Breaker for Feign clients
     * Prevents cascading failures when account-service is down or slow
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                // Timeout configuration
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5)) // Max 5 seconds per call
                        .build())
                // Circuit breaker configuration
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50) // Open circuit if 50% of calls fail
                        .waitDurationInOpenState(Duration.ofMillis(1000)) // Wait 1s before trying again
                        .slidingWindowSize(10) // Consider last 10 calls
                        .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 test calls when half-open
                        .build())
                .build());
    }
}
