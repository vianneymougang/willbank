package com.willbank.userservice.model;

/**
 * KYC (Know Your Customer) verification status for customer accounts.
 * Used to track the compliance verification process for banking customers.
 */
public enum KycStatus {
    /**
     * KYC verification is pending - newly created customer
     */
    PENDING,
    
    /**
     * KYC verification completed and approved
     */
    VERIFIED,
    
    /**
     * KYC verification rejected - customer needs to provide additional documents
     */
    REJECTED
}
