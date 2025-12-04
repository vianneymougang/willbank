// ============================================
// WILLBANK - API Endpoints
// ============================================

export const API_ENDPOINTS = {
  // Auth
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
    FORGOT_PASSWORD: '/auth/forgot-password',
    VERIFY_OTP: '/auth/verify-otp',
  },

  // Client Service
  CLIENTS: {
    ME: '/clients/me',
    UPDATE: '/clients/me',
  },

  // Account Service
  ACCOUNTS: {
    LIST: '/accounts',
    DETAIL: (id: string) => `/accounts/${id}`,
    STATEMENT: (id: string) => `/accounts/${id}/statement`,
  },

  // Transaction Service
  TRANSACTIONS: {
    LIST: '/transactions',
    DETAIL: (id: string) => `/transactions/${id}`,
    TRANSFER: '/transactions/transfer',
    BY_ACCOUNT: (accountId: string) => `/transactions/account/${accountId}`,
  },

  // Composite Service (Dashboard)
  COMPOSITE: {
    DASHBOARD: '/composite/dashboard',
    ACCOUNT_STATEMENT: (id: string) => `/composite/accounts/${id}/statement`,
    SEARCH_TRANSACTIONS: '/composite/transactions/search',
  },

  // Notification Service
  NOTIFICATIONS: {
    LIST: '/notifications',
    MARK_READ: (id: string) => `/notifications/${id}/read`,
    MARK_ALL_READ: '/notifications/read-all',
    FCM_REGISTER: '/notifications/fcm/register',
    FCM_UNREGISTER: '/notifications/fcm/unregister',
  },
} as const;
