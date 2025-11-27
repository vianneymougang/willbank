// ============================================
// WILLBANK - Dashboard Service
// ============================================

import { apiClient } from '../api/client';
import { API_ENDPOINTS } from '../api/endpoints';
import { Account, Transaction, DashboardData, User } from '@/src/core/entities';

export const dashboardService = {
  async getDashboard(): Promise<DashboardData> {
    // TODO: Replace with actual API call
    // return apiClient.get<DashboardData>(API_ENDPOINTS.COMPOSITE.DASHBOARD);
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Mock data for development
    return {
      user: {
        id: '1',
        firstName: 'Jean',
        lastName: 'Dupont',
        email: 'jean.dupont@email.com',
        phone: '+237 6 99 00 11 22',
        status: 'active',
        createdAt: '2024-01-15T10:00:00Z',
      },
      accounts: [
        {
          id: '1',
          userId: '1',
          accountNumber: 'WB001234567890',
          type: 'current',
          label: 'Compte Principal',
          balance: 1250000,
          currency: 'XAF',
          status: 'active',
          createdAt: '2024-01-15T10:00:00Z',
        },
        {
          id: '2',
          userId: '1',
          accountNumber: 'WB001234567891',
          type: 'savings',
          label: 'Épargne Projet',
          balance: 500000,
          currency: 'XAF',
          status: 'active',
          createdAt: '2024-02-01T10:00:00Z',
        },
      ],
      totalBalance: 1750000,
      recentTransactions: [
        {
          id: '1',
          accountId: '1',
          type: 'deposit',
          amount: 350000,
          currency: 'XAF',
          description: 'Salaire Novembre',
          status: 'completed',
          createdAt: '2024-11-25T08:30:00Z',
        },
        {
          id: '2',
          accountId: '1',
          type: 'payment',
          amount: -25000,
          currency: 'XAF',
          description: 'Supermarché Casino',
          merchantName: 'Casino Douala',
          status: 'completed',
          createdAt: '2024-11-24T14:20:00Z',
        },
        {
          id: '3',
          accountId: '1',
          type: 'transfer',
          amount: -50000,
          currency: 'XAF',
          description: 'Virement à Marie',
          recipientName: 'Marie Ngo',
          status: 'completed',
          createdAt: '2024-11-23T11:45:00Z',
        },
        {
          id: '4',
          accountId: '1',
          type: 'withdrawal',
          amount: -100000,
          currency: 'XAF',
          description: 'Retrait DAB Akwa',
          status: 'completed',
          createdAt: '2024-11-22T16:00:00Z',
        },
        {
          id: '5',
          accountId: '2',
          type: 'transfer',
          amount: 75000,
          currency: 'XAF',
          description: 'Épargne mensuelle',
          status: 'completed',
          createdAt: '2024-11-20T09:00:00Z',
        },
      ],
      unreadNotifications: 3,
    };
  },

  async getAccounts(): Promise<Account[]> {
    // TODO: Replace with actual API call
    // return apiClient.get<Account[]>(API_ENDPOINTS.ACCOUNTS.LIST);
    
    const dashboard = await this.getDashboard();
    return dashboard.accounts;
  },

  async getAccountDetail(accountId: string): Promise<Account> {
    // TODO: Replace with actual API call
    // return apiClient.get<Account>(API_ENDPOINTS.ACCOUNTS.DETAIL(accountId));
    
    const accounts = await this.getAccounts();
    const account = accounts.find(a => a.id === accountId);
    if (!account) throw new Error('Compte non trouvé');
    return account;
  },

  async getTransactions(accountId?: string): Promise<Transaction[]> {
    // TODO: Replace with actual API call
    
    const dashboard = await this.getDashboard();
    if (accountId) {
      return dashboard.recentTransactions.filter(t => t.accountId === accountId);
    }
    return dashboard.recentTransactions;
  },
};
