package com.willbank.userservice.audit;

/**
 * Enum representing audit actions for sensitive operations
 */
public enum AuditAction {
    // User Management
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_STATUS_CHANGED,

    // Account Management
    ACCOUNT_CREATED,
    ACCOUNT_UPDATED,
    ACCOUNT_DELETED,
    ACCOUNT_STATUS_CHANGED,
    ACCOUNT_ACCESSED,

    // Authentication & Security
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    PASSWORD_RESET,
    PASSWORD_CHANGED,
    TOKEN_REFRESHED,

    // Authorization
    UNAUTHORIZED_ACCESS_ATTEMPT,
    FORBIDDEN_ACTION_ATTEMPT
}
