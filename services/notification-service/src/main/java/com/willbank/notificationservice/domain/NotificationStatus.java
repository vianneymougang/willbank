package com.willbank.notificationservice.domain;

/**
 * Status of a notification
 */
public enum NotificationStatus {
    PENDING, // Created but not yet sent
    SENT, // Successfully sent
    FAILED, // Failed to send
    RETRYING // Currently retrying after failure
}
