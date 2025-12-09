package com.willbank.composite.service;

import com.willbank.composite.dto.DashboardResponse;

/**
 * Dashboard Service Interface
 */
public interface DashboardService {

    /**
     * Get customer dashboard with user info, accounts, and recent transactions
     * 
     * @param customerId Customer ID
     * @return Dashboard data
     */
    DashboardResponse getCustomerDashboard(Long customerId);
}
