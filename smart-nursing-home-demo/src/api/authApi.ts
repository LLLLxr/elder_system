import apiClient, { clearToken, setToken, unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type { LoginRequest, LoginResponse } from '../types/auth';
import { AUTH_LOGIN_PATH, AUTH_LOGOUT_PATH } from './endpoints';

const ADMIN_LOGIN_USER_STORAGE_KEY = 'smart_nursing_home_demo_admin_login_user';

type StoredAdminUser = {
  username: string | null;
  userId: number | null;
  permissions: string[];
  roles: string[];
};

function normalizeStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is string => typeof item === 'string' && item.length > 0);
}

function parseStoredAdminUser(raw: string): StoredAdminUser | null {
  try {
    const parsed = JSON.parse(raw) as {
      username?: string | null;
      userId?: number | null;
      permissions?: unknown;
      roles?: unknown;
    };
    return {
      username: parsed.username ?? null,
      userId: typeof parsed.userId === 'number' ? parsed.userId : null,
      permissions: normalizeStringArray(parsed.permissions),
      roles: normalizeStringArray(parsed.roles),
    };
  } catch {
    return {
      username: raw || null,
      userId: null,
      permissions: [],
      roles: [],
    };
  }
}

function persistCurrentAdminUser(user: StoredAdminUser) {
  if (user.username == null && user.userId == null && user.permissions.length === 0 && user.roles.length === 0) {
    localStorage.removeItem(ADMIN_LOGIN_USER_STORAGE_KEY);
    return;
  }
  localStorage.setItem(ADMIN_LOGIN_USER_STORAGE_KEY, JSON.stringify(user));
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>(AUTH_LOGIN_PATH, request);
  const data = unwrapApiData<LoginResponse>(response.data);

  if (data?.token) {
    setToken(data.token);
  }

  persistCurrentAdminUser({
    username: data?.userInfo?.username || request.username,
    userId: typeof data?.userInfo?.id === 'number' ? data.userInfo.id : null,
    permissions: normalizeStringArray(data?.permissions),
    roles: normalizeStringArray(data?.roles),
  });

  return data;
}

export function getCurrentAdminUser(): StoredAdminUser {
  const raw = localStorage.getItem(ADMIN_LOGIN_USER_STORAGE_KEY);
  if (!raw) {
    return { username: null, userId: null, permissions: [], roles: [] };
  }
  return parseStoredAdminUser(raw) ?? { username: null, userId: null, permissions: [], roles: [] };
}

export function getCurrentAdminUsername(): string | null {
  return getCurrentAdminUser().username;
}

export function setCurrentAdminUserContext(context: Partial<StoredAdminUser>) {
  const current = getCurrentAdminUser();
  persistCurrentAdminUser({
    username: context.username ?? current.username,
    userId: context.userId ?? current.userId,
    permissions: context.permissions ? normalizeStringArray(context.permissions) : current.permissions,
    roles: context.roles ? normalizeStringArray(context.roles) : current.roles,
  });
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post(AUTH_LOGOUT_PATH);
  } finally {
    clearToken();
    localStorage.removeItem(ADMIN_LOGIN_USER_STORAGE_KEY);
  }
}
