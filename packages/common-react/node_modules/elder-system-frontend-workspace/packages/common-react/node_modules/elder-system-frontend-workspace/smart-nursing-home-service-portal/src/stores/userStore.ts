import { useSyncExternalStore } from 'react';
import { getToken } from '../api/client';
import { getCurrentUserProfile } from '../api/currentUserApi';
import { getUserPermissionsByRole } from '../api/roleAdminApi';

const LOGIN_USER_STORAGE_KEY = 'smart_nursing_home_demo_login_user';

export const ASSESSMENT_REVIEW_PERMISSIONS = ['journey:assessment:approve', 'journey:assessment:reject'] as const;

type StoredUser = {
  username: string | null;
  userId: number | null;
  permissions: string[];
  roles: string[];
};

type UserState = {
  username: string | null;
  userId: number | null;
  permissions: string[];
  roles: string[];
};

let state: UserState = {
  username: null,
  userId: null,
  permissions: [],
  roles: [],
};

const listeners = new Set<() => void>();

function emitChange() {
  listeners.forEach((listener) => listener());
}

function decodeJwtUsername(token: string): string | null {
  try {
    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    const normalized = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padLength = (4 - (normalized.length % 4)) % 4;
    const payload = atob(normalized + '='.repeat(padLength));
    const data = JSON.parse(payload) as { sub?: string };
    return data.sub ?? null;
  } catch {
    return null;
  }
}

function normalizeStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is string => typeof item === 'string' && item.length > 0);
}

function parseStoredUser(raw: string): StoredUser | null {
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
    return { username: raw || null, userId: null, permissions: [], roles: [] };
  }
}

function resolveUserFromStorage(): StoredUser {
  const saved = localStorage.getItem(LOGIN_USER_STORAGE_KEY);
  if (saved) {
    return parseStoredUser(saved) ?? { username: null, userId: null };
  }

  const token = getToken();
  if (!token) {
    return { username: null, userId: null, permissions: [], roles: [] };
  }

  const username = decodeJwtUsername(token);
  const resolved = { username, userId: null, permissions: [], roles: [] };
  if (username) {
    localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(resolved));
  }
  return resolved;
}

function persistUser(user: StoredUser) {
  if (user.username == null && user.userId == null && user.permissions.length === 0 && user.roles.length === 0) {
    localStorage.removeItem(LOGIN_USER_STORAGE_KEY);
    return;
  }
  localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(user));
}

export function bootstrapUserStore() {
  const user = resolveUserFromStorage();
  state = {
    username: user.username,
    userId: user.userId,
    permissions: user.permissions,
    roles: user.roles,
  };
  emitChange();
}

type SetCurrentUserOptions = {
  permissions?: string[];
  roles?: string[];
};

export function setCurrentUser(username: string | null, userId: number | null = null, options: SetCurrentUserOptions = {}) {
  const permissions = options.permissions ? normalizeStringArray(options.permissions) : state.permissions;
  const roles = options.roles ? normalizeStringArray(options.roles) : state.roles;
  state = { username, userId, permissions, roles };
  persistUser({ username, userId, permissions, roles });
  emitChange();
}

export function setCurrentUsername(username: string | null) {
  setCurrentUser(username, state.userId);
}

export function clearCurrentUser() {
  setCurrentUser(null, null, { permissions: [], roles: [] });
}

export function getCurrentUsername() {
  return state.username;
}

export function getCurrentUserId() {
  return state.userId;
}

export function getCurrentPermissions() {
  return state.permissions;
}

export function hasPermission(permission: string) {
  return state.permissions.includes(permission);
}

export function hasAnyPermission(permissions: readonly string[]) {
  return permissions.some((permission) => state.permissions.includes(permission));
}

export function canAccessAssessmentReview() {
  return hasAnyPermission(ASSESSMENT_REVIEW_PERMISSIONS);
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function getSnapshot() {
  return state;
}

let ensureUserIdPromise: Promise<number | null> | null = null;
let ensurePermissionPromise: Promise<string[]> | null = null;

export async function ensureCurrentUserId(): Promise<number | null> {
  if (state.userId != null) {
    return state.userId;
  }

  if (!getToken()) {
    return null;
  }

  if (ensureUserIdPromise) {
    return ensureUserIdPromise;
  }

  ensureUserIdPromise = (async () => {
    try {
      const user = await getCurrentUserProfile();
      const username = user.username ?? state.username;
      const userId = typeof user.id === 'number' ? user.id : null;
      setCurrentUser(username, userId);
      return userId;
    } catch {
      return null;
    } finally {
      ensureUserIdPromise = null;
    }
  })();

  return ensureUserIdPromise;
}

export async function ensureAssessmentReviewPermissions(): Promise<string[]> {
  if (canAccessAssessmentReview()) {
    return state.permissions;
  }

  if (!getToken()) {
    return [];
  }

  const userId = await ensureCurrentUserId();
  if (userId == null) {
    return state.permissions;
  }

  if (ensurePermissionPromise) {
    return ensurePermissionPromise;
  }

  ensurePermissionPromise = (async () => {
    try {
      const permissionItems = await getUserPermissionsByRole(userId);
      const fetchedPermissions = permissionItems
        .map((item) => item.permissionCode)
        .filter((item): item is string => typeof item === 'string' && item.length > 0);
      if (fetchedPermissions.length > 0) {
        const mergedPermissions = Array.from(new Set([...state.permissions, ...fetchedPermissions]));
        setCurrentUser(state.username, userId, { permissions: mergedPermissions });
        return mergedPermissions;
      }
      return state.permissions;
    } catch {
      return state.permissions;
    } finally {
      ensurePermissionPromise = null;
    }
  })();

  return ensurePermissionPromise;
}

bootstrapUserStore();

export function useUserStore() {
  return useSyncExternalStore(subscribe, getSnapshot);
}
