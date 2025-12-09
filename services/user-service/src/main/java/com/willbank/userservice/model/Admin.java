package com.willbank.userservice.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

/**
 * Admin entity representing WillBank employees.
 * Admins have full access to the banking system for management purposes.
 */
@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Admin extends User {
    // Currently no additional fields specific to Admin
    // Can be extended in the future with:
    // - department
    // - employeeId
    // - permissions/roles granularity
}
