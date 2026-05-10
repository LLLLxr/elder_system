import axios, { AxiosError, AxiosInstance } from 'axios';

type ApiEnvelope<T> = {
  code?: number;
  message?: string;
  msg?: string;
  data?: T;
};

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

export function unwrapApiData<T>(payload: unknown): T {
  if (isObject(payload) && 'data' in payload) {
    const envelope = payload as ApiEnvelope<T>;
    if (envelope.data !== undefined) {
      return envelope.data;
    }
  }

  return payload as T;
}

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError;
    const data = axiosError.response?.data;

    if (typeof data === 'string' && data.trim()) {
      return data;
    }

    if (isObject(data)) {
      const maybeMessage = data.message;
      if (typeof maybeMessage === 'string' && maybeMessage.trim()) {
        return maybeMessage;
      }

      const maybeMsg = data.msg;
      if (typeof maybeMsg === 'string' && maybeMsg.trim()) {
        return maybeMsg;
      }

      const maybeError = data.error;
      if (isObject(maybeError)) {
        const nested = maybeError.message;
        if (typeof nested === 'string' && nested.trim()) {
          return nested;
        }
      }
    }

    if (typeof axiosError.message === 'string' && axiosError.message.trim()) {
      return axiosError.message;
    }
  }

  return fallback;
}

export interface ApiClientConfig {
  tokenStorageKey: string;
  loginPath: string;
  baseURL?: string;
  timeout?: number;
}

export function createTokenManager(storageKey: string) {
  return {
    getToken(): string | null {
      return localStorage.getItem(storageKey);
    },
    setToken(token: string): void {
      localStorage.setItem(storageKey, token);
    },
    clearToken(): void {
      localStorage.removeItem(storageKey);
    },
    hasToken(): boolean {
      return Boolean(localStorage.getItem(storageKey));
    },
  };
}

export function createApiClient(config: ApiClientConfig): AxiosInstance {
  const { tokenStorageKey, loginPath, baseURL, timeout = 15000 } = config;
  const tokenManager = createTokenManager(tokenStorageKey);

  const apiClient = axios.create({
    baseURL: baseURL ?? (import.meta.env.DEV ? '' : (import.meta.env.VITE_API_BASE_URL ?? '')),
    timeout,
  });

  apiClient.interceptors.request.use((requestConfig) => {
    const token = tokenManager.getToken();
    if (token) {
      requestConfig.headers.Authorization = `Bearer ${token}`;
    }
    return requestConfig;
  });

  apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error?.response?.status === 401) {
        tokenManager.clearToken();
        if (window.location.pathname !== loginPath) {
          window.location.href = loginPath;
        }
      }
      return Promise.reject(error);
    },
  );

  return apiClient;
}
