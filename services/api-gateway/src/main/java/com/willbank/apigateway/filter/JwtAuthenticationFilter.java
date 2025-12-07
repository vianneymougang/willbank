package com.willbank.apigateway.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter for JWT authentication on API Gateway
 * Validates JWT tokens with auth-service for protected routes
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/api/users/register",
            "/api/users/change-password-first-login",
            "/actuator/");

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Allow public paths without authentication
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Validate token with auth-service
        return validateToken(authHeader)
                .flatMap(validationResponse -> {
                    if (validationResponse != null && validationResponse.isValid()) {
                        log.debug("Token validated successfully for path: {} - User: {}", path,
                                validationResponse.getEmail());

                        // Propagate user information to downstream services via headers
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(builder -> builder
                                        .header("X-User-Id", String.valueOf(validationResponse.getUserId()))
                                        .header("X-User-Email", validationResponse.getEmail())
                                        .header("X-User-Role", validationResponse.getRole()))
                                .build();

                        return chain.filter(modifiedExchange);
                    } else {
                        log.warn("Invalid token for path: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error validating token: {}", error.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<ValidationResponse> validateToken(String authHeader) {
        return webClientBuilder.build()
                .get()
                .uri("lb://auth-service/api/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(ValidationResponse.class)
                .onErrorReturn(new ValidationResponse()); // Return invalid response on error
    }

    @Override
    public int getOrder() {
        return -100; // High priority to run before other filters
    }

    // DTO for validation response
    @Setter
    @Getter
    public static class ValidationResponse {
        private boolean valid;
        private Long userId;
        private String email;
        private String role;

        public ValidationResponse() {
        }

    }
}
