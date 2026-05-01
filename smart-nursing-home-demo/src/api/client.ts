import axios, { AxiosError } from 'axios';
import { ROUTE_PATHS } from '../constants/routes';

const TOKEN_STORAGE_KEY = 'smart_nursing_home_demo_token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export function hasToken(): boolean {
  return Boolean(getToken());
}

const baseURL = import.meta.env.DEV ? '' : (import.meta.env.VITE_API_BASE_URL ?? '');

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

const apiClient = axios.create({
  baseURL,
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearToken();
      if (window.location.pathname !== ROUTE_PATHS.LOGIN) {
        window.location.href = ROUTE_PATHS.LOGIN;
      }
    }
    return Promise.reject(error);
  },
);

export default apiClient;
