package com.willbank.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic Spring Boot application context test
 * Ensures the application starts successfully
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // If the context fails to load, this test will fail
    }
}
