package com.willbank.servicetransaction.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8081/api/accounts";
    
    public BigDecimal getAccountBalance(String accountId) {
        try {
            String url = ACCOUNT_SERVICE_URL + "/" + accountId + "/balance";
            return restTemplate.getForObject(url, BigDecimal.class);
        } catch (Exception e) {
            log.error("Error fetching balance for account: {}", accountId, e);
            return BigDecimal.ZERO;
        }
    }
    
    public boolean accountExists(String accountId) {
        try {
            String url = ACCOUNT_SERVICE_URL + "/" + accountId + "/exists";
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking if account exists: {}", accountId, e);
            return false;
        }
    }
}
