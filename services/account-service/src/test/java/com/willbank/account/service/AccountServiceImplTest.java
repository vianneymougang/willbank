package com.willbank.account.service;

import com.willbank.account.domain.Account;
import com.willbank.account.domain.AccountRepository;
import com.willbank.account.domain.AccountStatus;
import com.willbank.account.domain.AccountType;
import com.willbank.account.dto.AccountRequest;
import com.willbank.account.dto.AccountResponse;
import com.willbank.account.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private AccountRequest testRequest;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .customerId(100L)
                .accountNumber("WB12345678901234")
                .type(AccountType.CURRENT)
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .label("Compte Principal")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = AccountRequest.builder()
                .customerId(100L)
                .type(AccountType.CURRENT)
                .label("Compte Principal")
                .initialBalance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void createAccount_ShouldReturnAccountResponse() {
        // Given
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse response = accountService.createAccount(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(testAccount.getId(), response.getId());
        assertEquals(testAccount.getCustomerId(), response.getCustomerId());
        assertEquals(testAccount.getType(), response.getType());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenExists() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(authorizationService.canAccessAccount(any(Account.class))).thenReturn(true);

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testAccount.getId(), response.getId());
        assertEquals(testAccount.getAccountNumber(), response.getAccountNumber());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountById_ShouldThrowException_WhenNotExists() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> accountService.getAccountById(999L));
        verify(accountRepository, times(1)).findById(999L);
    }

    @Test
    void getAllAccounts_ShouldReturnListOfAccounts() {
        // Given
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountRepository.findAll()).thenReturn(accounts);

        // When
        List<AccountResponse> responses = accountService.getAllAccounts();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void getAccountsByCustomerId_ShouldReturnCustomerAccounts() {
        // Given
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountRepository.findByCustomerId(100L)).thenReturn(accounts);

        // When
        List<AccountResponse> responses = accountService.getAccountsByCustomerId(100L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getCustomerId());
        verify(accountRepository, times(1)).findByCustomerId(100L);
    }

    @Test
    void updateAccountStatus_ShouldChangeStatus() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse response = accountService.updateAccountStatus(1L, AccountStatus.BLOCKED);

        // Then
        assertNotNull(response);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void deleteAccount_ShouldThrowException_WhenBalanceNotZero() {
        // Given
        testAccount.setBalance(BigDecimal.valueOf(100));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThrows(RuntimeException.class, () -> accountService.deleteAccount(1L));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void deleteAccount_ShouldSucceed_WhenBalanceIsZero() {
        // Given
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    void getAccountBalance_ShouldReturnBalance() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        BigDecimal balance = accountService.getAccountBalance(1L);

        // Then
        assertNotNull(balance);
        assertEquals(BigDecimal.valueOf(1000), balance);
        verify(accountRepository, times(1)).findById(1L);
    }
}
