// ============================================
// WILLBANK - API Client
// ============================================

import { API_CONFIG } from '@/src/config/constants';

interface RequestConfig {
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  endpoint: string;
  body?: Record<string, unknown>;
  headers?: Record<string, string>;
  params?: Record<string, string>;
}

interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

class ApiClient {
  private baseUrl: string;
  private accessToken: string | null = null;

  constructor() {
    this.baseUrl = API_CONFIG.baseUrl;
  }

  setAccessToken(token: string | null) {
    this.accessToken = token;
  }

  private buildUrl(endpoint: string, params?: Record<string, string>): string {
    const url = new URL(endpoint, this.baseUrl);
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        url.searchParams.append(key, value);
      });
    }
    return url.toString();
  }

  private async request<T>(config: RequestConfig): Promise<ApiResponse<T>> {
    const { method, endpoint, body, headers, params } = config;

    const url = this.buildUrl(endpoint, params);

    const requestHeaders: Record<string, string> = {
      'Content-Type': 'application/json',
      ...headers,
    };

    if (this.accessToken) {
      requestHeaders['Authorization'] = `Bearer ${this.accessToken}`;
    }

    try {
      const response = await fetch(url, {
        method,
        headers: requestHeaders,
        body: body ? JSON.stringify(body) : undefined,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new ApiError(
          data.message || 'Une erreur est survenue',
          response.status,
          data
        );
      }

      return {
        data,
        status: response.status,
      };
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError('Erreur de connexion au serveur', 0);
    }
  }

  async get<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const response = await this.request<T>({ method: 'GET', endpoint, params });
    return response.data;
  }

  async post<T>(endpoint: string, body?: Record<string, unknown>): Promise<T> {
    const response = await this.request<T>({ method: 'POST', endpoint, body });
    return response.data;
  }

  async put<T>(endpoint: string, body?: Record<string, unknown>): Promise<T> {
    const response = await this.request<T>({ method: 'PUT', endpoint, body });
    return response.data;
  }

  async patch<T>(endpoint: string, body?: Record<string, unknown>): Promise<T> {
    const response = await this.request<T>({ method: 'PATCH', endpoint, body });
    return response.data;
  }

  async delete<T>(endpoint: string): Promise<T> {
    const response = await this.request<T>({ method: 'DELETE', endpoint });
    return response.data;
  }
}

export class ApiError extends Error {
  status: number;
  data?: unknown;

  constructor(message: string, status: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export const apiClient = new ApiClient();
