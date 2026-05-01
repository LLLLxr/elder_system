import apiClient, { clearToken, setToken, unwrapApiData } from './client';
import type { LoginRequest, LoginResponse } from '../types/auth';
import { AUTH_LOGIN_PATH, AUTH_LOGOUT_PATH } from './endpoints';

const ADMIN_LOGIN_USER_STORAGE_KEY = 'smart_nursing_home_demo_admin_login_user';

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post(AUTH_LOGIN_PATH, request);
  const data = unwrapApiData<LoginResponse>(response.data);

  if (data?.token) {
    setToken(data.token);
  }

  const username = data?.userInfo?.username || request.username;
  localStorage.setItem(ADMIN_LOGIN_USER_STORAGE_KEY, username);

  return data;
}

export function getCurrentAdminUsername(): string | null {
  return localStorage.getItem(ADMIN_LOGIN_USER_STORAGE_KEY);
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post(AUTH_LOGOUT_PATH);
  } finally {
    clearToken();
    localStorage.removeItem(ADMIN_LOGIN_USER_STORAGE_KEY);
  }
}
