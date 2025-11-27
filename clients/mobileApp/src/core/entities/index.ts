// ============================================
// WILLBANK - Domain Entities
// ============================================

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  avatar?: string;
  status: 'active' | 'pending' | 'blocked';
  createdAt: string;
}

export interface Account {
  id: string;
  userId: string;
  accountNumber: string;
  type: 'current' | 'savings';
  label: string;
  balance: number;
  currency: string;
  status: 'active' | 'blocked';
  createdAt: string;
}

export interface Transaction {
  id: string;
  accountId: string;
  type: 'transfer' | 'deposit' | 'withdrawal' | 'payment';
  amount: number;
  currency: string;
  description: string;
  status: 'pending' | 'completed' | 'failed';
  recipientName?: string;
  recipientAccount?: string;
  merchantName?: string;
  category?: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  title: string;
  body: string;
  type: 'transaction' | 'security' | 'promo' | 'info';
  read: boolean;
  data?: Record<string, unknown>;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface DashboardData {
  user: User;
  accounts: Account[];
  totalBalance: number;
  recentTransactions: Transaction[];
  unreadNotifications: number;
}
