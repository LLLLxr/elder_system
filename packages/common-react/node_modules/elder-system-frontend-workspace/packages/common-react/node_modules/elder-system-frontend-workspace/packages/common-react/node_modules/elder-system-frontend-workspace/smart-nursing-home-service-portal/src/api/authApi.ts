import apiClient, { clearToken, setToken, unwrapApiData } from './client';
import type { LoginRequest, LoginResponse } from '../types/auth';
import { AUTH_LOGIN_PATH, AUTH_LOGOUT_PATH } from './endpoints';
import { clearCurrentUser, getCurrentUsername, setCurrentUser } from '../stores/userStore';

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post(AUTH_LOGIN_PATH, request);
  const data = unwrapApiData<LoginResponse>(response.data);

  if (data?.token) {
    setToken(data.token);
  }

  const username = data?.userInfo?.username || request.username;
  const userId = data?.userInfo?.id ?? null;
  setCurrentUser(username, userId, {
    permissions: data?.permissions ?? [],
    roles: data?.roles ?? [],
  });

  return data;
}

export function getLoginUsername(): string | null {
  return getCurrentUsername();
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post(AUTH_LOGOUT_PATH);
  } finally {
    clearToken();
    clearCurrentUser();
    sessionStorage.removeItem('journeyResult');
    sessionStorage.removeItem('journeyContext');
  }
}
