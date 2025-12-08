# Intégration avec l'API Gateway

## Architecture

```
┌──────────────┐      ┌──────────────────┐      ┌─────────────────────┐
│   Frontend   │─────▶│   API Gateway    │─────▶│ Notification Service│
│   Angular    │      │   (Port 8080)    │      │   (Port 8082)       │
│              │      │                  │      │                     │
│              │      │ - Routing        │      │ - Email Service     │
│              │      │ - Auth           │      │ - Push Service      │
│              │      │ - Load Balancing │      │ - RabbitMQ Listener │
└──────────────┘      └──────────────────┘      └─────────────────────┘
```

## Configuration de l'API Gateway

### Spring Cloud Gateway

#### 1. Dépendances (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 2. Configuration (application.yml)

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Route pour le microservice de notifications
        - id: notification-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/notifications/**
          filters:
            - AddRequestHeader=X-Service-Id, notification-service
            - AddRequestHeader=X-Gateway-Request, true
            - name: CircuitBreaker
              args:
                name: notificationServiceCircuitBreaker
                fallbackUri: forward:/fallback/notifications
        
        # Route pour les WebSockets (si nécessaire)
        - id: notification-websocket
          uri: ws://localhost:8082
          predicates:
            - Path=/ws/notifications/**

      # Configuration CORS globale
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
              - "https://your-frontend-domain.com"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

      # Configuration du circuit breaker
      default-filters:
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
            methods: GET,POST
            backoff:
              firstBackoff: 50ms
              maxBackoff: 500ms

# Configuration Eureka (si utilisé)
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true

# Configuration Resilience4j
resilience4j:
  circuitbreaker:
    instances:
      notificationServiceCircuitBreaker:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

#### 3. Contrôleur de Fallback

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, String>> notificationsFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Le service de notifications est temporairement indisponible");
        response.put("status", "SERVICE_UNAVAILABLE");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

### Kong API Gateway

#### 1. Configuration du Service

```bash
# Ajouter le service
curl -i -X POST http://localhost:8001/services/ \
  --data name=notification-service \
  --data url='http://localhost:8082'

# Ajouter la route
curl -i -X POST http://localhost:8001/services/notification-service/routes \
  --data 'paths[]=/api/notifications' \
  --data name=notification-route
```

#### 2. Plugins

```bash
# Rate Limiting
curl -X POST http://localhost:8001/services/notification-service/plugins \
  --data "name=rate-limiting" \
  --data "config.minute=100" \
  --data "config.hour=1000"

# CORS
curl -X POST http://localhost:8001/services/notification-service/plugins \
  --data "name=cors" \
  --data "config.origins=http://localhost:4200" \
  --data "config.methods=GET,POST,PUT,DELETE" \
  --data "config.credentials=true"

# JWT Authentication
curl -X POST http://localhost:8001/services/notification-service/plugins \
  --data "name=jwt"
```

### Nginx API Gateway

```nginx
upstream notification_service {
    server localhost:8082;
    # Pour load balancing
    # server localhost:8083;
    # server localhost:8084;
}

server {
    listen 8080;
    server_name api-gateway;

    # CORS
    add_header 'Access-Control-Allow-Origin' 'http://localhost:4200' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
    add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type' always;
    add_header 'Access-Control-Allow-Credentials' 'true' always;

    # Route vers le microservice de notifications
    location /api/notifications/ {
        proxy_pass http://notification_service/api/notifications/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Service-Id notification-service;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

## Sécurité

### 1. Authentification JWT

Le microservice peut valider les tokens JWT de l'API Gateway:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/notifications/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("http://auth-server/jwks").build();
    }
}
```

### 2. API Key

```java
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    
    @Value("${api.gateway.key}")
    private String apiGatewayKey;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null || !apiKey.equals(apiGatewayKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## Service Discovery

### Eureka

```yaml
# Dans notification-service/application.yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    registerWithEureka: true
    fetchRegistry: true
  instance:
    instanceId: ${spring.application.name}:${random.value}
    preferIpAddress: true
    leaseRenewalIntervalInSeconds: 10
    metadataMap:
      version: 1.0.0
      description: Microservice de gestion des notifications
```

### Consul

```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        serviceName: notification-service
        healthCheckPath: /actuator/health
        healthCheckInterval: 10s
```

## Load Balancing

### Configuration côté Gateway

```java
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Utilisation

```java
@Service
public class NotificationClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void sendNotification(NotificationRequest request) {
        // L'URL utilise le nom du service au lieu de l'IP
        String url = "http://notification-service/api/notifications/history/send";
        restTemplate.postForObject(url, request, NotificationResponse.class);
    }
}
```

## Monitoring et Tracing

### Distributed Tracing (Zipkin/Jaeger)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0
  zipkin:
    base-url: http://localhost:9411
```

## Tests d'Intégration

### Test avec l'API Gateway

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GatewayIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testSendNotificationThroughGateway() throws Exception {
        mockMvc.perform(post("/api/notifications/history/send")
                .header("X-API-Key", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipient\":\"test@example.com\",\"subject\":\"Test\",\"content\":\"Test\",\"type\":\"EMAIL\"}"))
                .andExpect(status().isCreated());
    }
}
```

## Configuration Frontend

### Service Angular avec API Gateway

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  // Utiliser l'URL de l'API Gateway au lieu du microservice directement
  private apiUrl = `${environment.apiGatewayUrl}/api/notifications/history`;

  constructor(private http: HttpClient) {}

  sendNotification(request: NotificationRequest) {
    return this.http.post(`${this.apiUrl}/send`, request);
  }
}
```

### Environment

```typescript
// environment.ts
export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8080'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiGatewayUrl: 'https://api.your-domain.com'
};
```

## Checklist d'Intégration

- [ ] API Gateway configuré et démarré
- [ ] Routes configurées vers le microservice
- [ ] CORS configuré correctement
- [ ] Authentification/Autorisation en place
- [ ] Circuit breaker configuré
- [ ] Rate limiting configuré
- [ ] Service discovery configuré (si utilisé)
- [ ] Monitoring et tracing activés
- [ ] Tests d'intégration passent
- [ ] Documentation à jour
