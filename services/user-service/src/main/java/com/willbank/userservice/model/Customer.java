package com.willbank.userservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Customer entity representing banking clients.
 * Customers must complete KYC (Know Your Customer) verification.
 */
@Entity
@DiscriminatorValue("CUSTOMER")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Customer extends User {

    /**
     * KYC verification status for regulatory compliance.
     * Initialized to PENDING for new customers.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = true, length = 20)
    private KycStatus kycStatus;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (kycStatus == null) {
            kycStatus = KycStatus.PENDING;
        }
    }
}
