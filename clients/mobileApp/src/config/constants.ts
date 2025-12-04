// ============================================
// WILLBANK - App Configuration
// ============================================

export const APP_CONFIG = {
  name: 'WillBank',
  version: '1.0.0',
  currency: 'XAF', // Franc CFA
  currencySymbol: 'FCFA',
};

export const API_CONFIG = {
  baseUrl: __DEV__ 
    ? 'http://10.0.2.2:8080/api' // Android emulator
    : 'https://api.willbank.com',
  timeout: 30000,
};

export const STORAGE_KEYS = {
  AUTH_TOKENS: '@willbank/auth_tokens',
  USER_DATA: '@willbank/user_data',
  FCM_TOKEN: '@willbank/fcm_token',
  THEME: '@willbank/theme',
  ONBOARDING_DONE: '@willbank/onboarding_done',
};

export const TRANSACTION_TYPES = {
  transfer: { label: 'Virement', icon: 'swap-horiz', color: '#3B82F6' },
  deposit: { label: 'Dépôt', icon: 'arrow-downward', color: '#10B981' },
  withdrawal: { label: 'Retrait', icon: 'arrow-upward', color: '#F59E0B' },
  payment: { label: 'Paiement', icon: 'shopping-cart', color: '#8B5CF6' },
} as const;

export const ACCOUNT_TYPES = {
  current: { label: 'Compte Courant', icon: 'account-balance-wallet' },
  savings: { label: 'Compte Épargne', icon: 'savings' },
} as const;
