package com.willbank.notificationservice.domain;

/**
 * Types of notifications sent by the system
 */
public enum NotificationType {
    // Customer lifecycle
    CUSTOMER_WELCOME,

    // Account events
    ACCOUNT_CREATED,
    ACCOUNT_STATUS_CHANGED,

    // Transaction events (prepared for future)
    TRANSACTION_COMPLETED,
    LARGE_TRANSACTION_ALERT,
    INSUFFICIENT_FUNDS,

    // KYC events
    KYC_STATUS_CHANGED,

    // Security events
    SUSPICIOUS_ACTIVITY,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,

    // Operational
    LOW_BALANCE_ALERT,
    DIRECT_DEPOSIT_RECEIVED
}
