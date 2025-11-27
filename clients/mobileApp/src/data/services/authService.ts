// ============================================
// WILLBANK - Auth Service
// ============================================

import { apiClient } from '../api/client';
import { API_ENDPOINTS } from '../api/endpoints';
import { User, AuthTokens } from '@/src/core/entities';

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
}

interface LoginResponse {
  user: User;
  tokens: AuthTokens;
}

interface ForgotPasswordRequest {
  email: string;
}

interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    // TODO: Replace with actual API call
    // return apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.LOGIN, credentials);
    
    // Mock response for development
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    return {
      user: {
        id: '1',
        firstName: 'Jean',
        lastName: 'Dupont',
        email: credentials.email,
        phone: '+237 6 99 00 11 22',
        status: 'active',
        createdAt: new Date().toISOString(),
      },
      tokens: {
        accessToken: 'mock_access_token',
        refreshToken: 'mock_refresh_token',
        expiresAt: Date.now() + 3600000,
      },
    };
  },

  async register(data: RegisterRequest): Promise<LoginResponse> {
    // TODO: Replace with actual API call
    // return apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.REGISTER, data);
    
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    return {
      user: {
        id: '1',
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        phone: data.phone,
        status: 'pending',
        createdAt: new Date().toISOString(),
      },
      tokens: {
        accessToken: 'mock_access_token',
        refreshToken: 'mock_refresh_token',
        expiresAt: Date.now() + 3600000,
      },
    };
  },

  async forgotPassword(data: ForgotPasswordRequest): Promise<{ message: string }> {
    // TODO: Replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    return { message: 'Code envoyé par email' };
  },

  async verifyOtp(data: VerifyOtpRequest): Promise<{ valid: boolean }> {
    // TODO: Replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    return { valid: data.otp === '123456' };
  },

  async logout(): Promise<void> {
    // TODO: Replace with actual API call
    // return apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
    await new Promise(resolve => setTimeout(resolve, 500));
  },

  async refreshToken(refreshToken: string): Promise<AuthTokens> {
    // TODO: Replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 500));
    return {
      accessToken: 'new_mock_access_token',
      refreshToken: 'new_mock_refresh_token',
      expiresAt: Date.now() + 3600000,
    };
  },
};
