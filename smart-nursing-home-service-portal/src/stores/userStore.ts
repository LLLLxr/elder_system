import { useSyncExternalStore } from 'react';
import { getToken } from '../api/client';
import { getCurrentUserProfile, listMyElderBindings } from '../api/currentUserApi';
import { getUserPermissionsByRole } from '../api/roleAdminApi';
import type { ElderBindingItem } from '../types/admin';

const LOGIN_USER_STORAGE_KEY = 'smart_nursing_home_demo_login_user';
const ACTIVE_ELDER_ID_STORAGE_KEY = 'smart_nursing_home_demo_active_elder_id';

export const ASSESSMENT_REVIEW_PERMISSIONS = ['journey:assessment:approve', 'journey:assessment:reject'] as const;
export const FAMILY_VISIT_PERMISSIONS = [
  'admission:family-visit-slot:read',
  'admission:family-visit-reservation:create',
  'admission:family-visit-reservation:my:list',
] as const;
export const CAREGIVER_QUALIFICATION_PERMISSIONS = [
  'quality:caregiver-qualification:create',
  'quality:caregiver-qualification:my:list',
] as const;
export const CAREGIVER_DAILY_TASK_PERMISSIONS = [
  'care-delivery:daily-task:list',
  'care-delivery:daily-task:check-in',
  'care-delivery:check-in:my:list',
] as const;
export const FAMILY_CARE_DELIVERY_PERMISSIONS = [
  'care-delivery:family-service-plan:list',
  'care-delivery:family-check-in:list',
  'care-delivery:family-nurse-care-record:list',
  'health:family-doctor-round-record:list',
] as const;
export const ELDER_BINDING_PERMISSIONS = [
  'elder-binding:list',
  'elder-binding:request:create',
  'elder-binding:request:my:list',
  'elder-binding:self:bind',
] as const;

export const ACTIVE_ELDER_REQUIRED_MESSAGE = '请先在“绑定老人”中选择当前服务对象';

function normalizeNumber(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}

function normalizeStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is string => typeof item === 'string' && item.length > 0);
}

function normalizeElderBindings(value: unknown): ElderBindingItem[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is ElderBindingItem => {
    if (typeof item !== 'object' || item == null) {
      return false;
    }
    const binding = item as ElderBindingItem;
    return typeof binding.elderId === 'number' && Number.isFinite(binding.elderId);
  });
}

type StoredUser = {
  username: string | null;
  userId: number | null;
  permissions: string[];
  roles: string[];
  elderBindings: ElderBindingItem[];
  activeElderId: number | null;
};

type UserState = {
  username: string | null;
  userId: number | null;
  permissions: string[];
  roles: string[];
  elderBindings: ElderBindingItem[];
  activeElderId: number | null;
};

let state: UserState = {
  username: null,
  userId: null,
  permissions: [],
  roles: [],
  elderBindings: [],
  activeElderId: null,
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

function readStoredActiveElderId(): number | null {
  const raw = localStorage.getItem(ACTIVE_ELDER_ID_STORAGE_KEY);
  if (!raw) {
    return null;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function resolveActiveElderId(bindings: ElderBindingItem[], preferredActiveElderId?: number | null): number | null {
  const candidate = preferredActiveElderId ?? readStoredActiveElderId();
  if (candidate != null && bindings.some((binding) => binding.elderId === candidate)) {
    return candidate;
  }
  const firstBinding = bindings[0];
  return typeof firstBinding?.elderId === 'number' ? firstBinding.elderId : null;
}

function persistActiveElderId(activeElderId: number | null) {
  if (activeElderId == null) {
    localStorage.removeItem(ACTIVE_ELDER_ID_STORAGE_KEY);
    return;
  }
  localStorage.setItem(ACTIVE_ELDER_ID_STORAGE_KEY, String(activeElderId));
}

function parseStoredUser(raw: string): StoredUser | null {
  try {
    const parsed = JSON.parse(raw) as {
      username?: string | null;
      userId?: number | null;
      permissions?: unknown;
      roles?: unknown;
      elderBindings?: unknown;
      activeElderId?: number | null;
    };
    const elderBindings = normalizeElderBindings(parsed.elderBindings);
    return {
      username: parsed.username ?? null,
      userId: normalizeNumber(parsed.userId),
      permissions: normalizeStringArray(parsed.permissions),
      roles: normalizeStringArray(parsed.roles),
      elderBindings,
      activeElderId: resolveActiveElderId(elderBindings, normalizeNumber(parsed.activeElderId)),
    };
  } catch {
    return {
      username: raw || null,
      userId: null,
      permissions: [],
      roles: [],
      elderBindings: [],
      activeElderId: readStoredActiveElderId(),
    };
  }
}

function resolveUserFromStorage(): StoredUser {
  const saved = localStorage.getItem(LOGIN_USER_STORAGE_KEY);
  if (saved) {
    return parseStoredUser(saved) ?? {
      username: null,
      userId: null,
      permissions: [],
      roles: [],
      elderBindings: [],
      activeElderId: null,
    };
  }

  const token = getToken();
  if (!token) {
    return { username: null, userId: null, permissions: [], roles: [], elderBindings: [], activeElderId: null };
  }

  const username = decodeJwtUsername(token);
  const resolved = {
    username,
    userId: null,
    permissions: [],
    roles: [],
    elderBindings: [],
    activeElderId: readStoredActiveElderId(),
  };
  if (username) {
    localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(resolved));
  }
  return resolved;
}

function persistUser(user: StoredUser) {
  if (
    user.username == null &&
    user.userId == null &&
    user.permissions.length === 0 &&
    user.roles.length === 0 &&
    user.elderBindings.length === 0 &&
    user.activeElderId == null
  ) {
    localStorage.removeItem(LOGIN_USER_STORAGE_KEY);
    localStorage.removeItem(ACTIVE_ELDER_ID_STORAGE_KEY);
    return;
  }
  localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(user));
  persistActiveElderId(user.activeElderId);
}

export function bootstrapUserStore() {
  const user = resolveUserFromStorage();
  state = {
    username: user.username,
    userId: user.userId,
    permissions: user.permissions,
    roles: user.roles,
    elderBindings: user.elderBindings,
    activeElderId: user.activeElderId,
  };
  emitChange();
}

type SetCurrentUserOptions = {
  permissions?: string[];
  roles?: string[];
  elderBindings?: ElderBindingItem[];
  activeElderId?: number | null;
};

export function setCurrentUser(username: string | null, userId: number | null = null, options: SetCurrentUserOptions = {}) {
  const permissions = options.permissions ? normalizeStringArray(options.permissions) : state.permissions;
  const roles = options.roles ? normalizeStringArray(options.roles) : state.roles;
  const elderBindings = options.elderBindings ? normalizeElderBindings(options.elderBindings) : state.elderBindings;
  const activeElderId = resolveActiveElderId(elderBindings, options.activeElderId ?? state.activeElderId);
  state = { username, userId, permissions, roles, elderBindings, activeElderId };
  persistUser({ username, userId, permissions, roles, elderBindings, activeElderId });
  emitChange();
}

export function setCurrentUsername(username: string | null) {
  setCurrentUser(username, state.userId);
}

export function clearCurrentUser() {
  setCurrentUser(null, null, { permissions: [], roles: [], elderBindings: [], activeElderId: null });
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

export function getCurrentElderBindings() {
  return state.elderBindings;
}

export function getActiveElderId() {
  return state.activeElderId;
}

export function getActiveElderBinding() {
  return state.elderBindings.find((binding) => binding.elderId === state.activeElderId) ?? null;
}

export function getActiveElderLabel() {
  const activeBinding = getActiveElderBinding();
  if (!activeBinding) {
    return null;
  }
  return activeBinding.elderName?.trim() || `老人 ${activeBinding.elderId ?? '-'}`;
}

export function setActiveElderId(elderId: number | null) {
  const nextActiveElderId = resolveActiveElderId(state.elderBindings, elderId);
  state = {
    ...state,
    activeElderId: nextActiveElderId,
  };
  persistUser({
    username: state.username,
    userId: state.userId,
    permissions: state.permissions,
    roles: state.roles,
    elderBindings: state.elderBindings,
    activeElderId: nextActiveElderId,
  });
  emitChange();
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

export function canAccessFamilyVisit() {
  return hasAnyPermission(FAMILY_VISIT_PERMISSIONS);
}

export function canAccessCaregiverQualification() {
  return hasAnyPermission(CAREGIVER_QUALIFICATION_PERMISSIONS);
}

export function canAccessCaregiverDailyTasks() {
  return hasAnyPermission(CAREGIVER_DAILY_TASK_PERMISSIONS);
}

export function canAccessFamilyCareDelivery() {
  return hasAnyPermission(FAMILY_CARE_DELIVERY_PERMISSIONS);
}

export function canAccessElderBinding() {
  return hasAnyPermission(ELDER_BINDING_PERMISSIONS);
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
let ensureElderBindingsPromise: Promise<ElderBindingItem[]> | null = null;

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
      setCurrentUser(username, userId, {
        elderBindings: user.elderBindings ?? state.elderBindings,
      });
      return userId;
    } catch {
      return null;
    } finally {
      ensureUserIdPromise = null;
    }
  })();

  return ensureUserIdPromise;
}

export async function ensureElderBindings(): Promise<ElderBindingItem[]> {
  if (!getToken()) {
    return [];
  }

  await ensureCurrentUserId();

  if (state.elderBindings.length > 0) {
    return state.elderBindings;
  }

  return refreshElderBindings();
}

export async function refreshElderBindings(): Promise<ElderBindingItem[]> {
  if (!getToken()) {
    return [];
  }

  if (ensureElderBindingsPromise) {
    return ensureElderBindingsPromise;
  }

  ensureElderBindingsPromise = (async () => {
    try {
      const bindings = await listMyElderBindings();
      setCurrentUser(state.username, state.userId, {
        elderBindings: bindings,
      });
      return bindings;
    } catch {
      return state.elderBindings;
    } finally {
      ensureElderBindingsPromise = null;
    }
  })();

  return ensureElderBindingsPromise;
}

export async function ensureActiveElderId(): Promise<number | null> {
  if (state.activeElderId != null) {
    return state.activeElderId;
  }
  const bindings = await ensureElderBindings();
  const activeElderId = resolveActiveElderId(bindings, state.activeElderId);
  if (activeElderId !== state.activeElderId) {
    setActiveElderId(activeElderId);
  }
  return activeElderId;
}

export async function ensurePermissions(): Promise<string[]> {
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

export async function ensureAssessmentReviewPermissions(): Promise<string[]> {
  if (canAccessAssessmentReview()) {
    return state.permissions;
  }
  return ensurePermissions();
}

bootstrapUserStore();

export function useUserStore() {
  return useSyncExternalStore(subscribe, getSnapshot);
}
