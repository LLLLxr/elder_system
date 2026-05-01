import apiClient, { unwrapApiData } from './client';
import type { PermissionItem, RoleItem } from '../types/admin';
import {
  ROLE_ASSIGN_PERMISSION_PATH,
  ROLE_ASSIGN_USER_PATH,
  ROLE_LIST_PATH,
  ROLE_REMOVE_PERMISSION_PATH,
  ROLE_REMOVE_USER_PATH,
  ROLE_USER_PATH,
  ROLE_WITH_PERMISSIONS_PATH,
} from './endpoints';

export async function getRoles(keyword?: string): Promise<RoleItem[]> {
  const response = await apiClient.get<RoleItem[] | { data?: RoleItem[] }>(ROLE_LIST_PATH, { params: { keyword } });
  const payload = unwrapApiData<RoleItem[]>(response.data);
  return Array.isArray(payload) ? payload : [];
}

export async function getRolesWithPermissions(): Promise<RoleItem[]> {
  const response = await apiClient.get<RoleItem[] | { data?: RoleItem[] }>(ROLE_WITH_PERMISSIONS_PATH);
  const payload = unwrapApiData<RoleItem[]>(response.data);
  return Array.isArray(payload) ? payload : [];
}

export async function getUserRoles(userId: number): Promise<string[]> {
  const response = await apiClient.get<string[]>(ROLE_USER_PATH(userId));
  return response.data;
}

export async function assignRoleToUser(userId: number, roleId: number): Promise<boolean> {
  const response = await apiClient.post<boolean>(ROLE_ASSIGN_USER_PATH(userId, roleId));
  return response.data;
}

export async function removeRoleFromUser(userId: number, roleId: number): Promise<boolean> {
  const response = await apiClient.delete<boolean>(ROLE_REMOVE_USER_PATH(userId, roleId));
  return response.data;
}

export async function assignPermissionToRole(roleId: number, permissionId: number): Promise<boolean> {
  const response = await apiClient.post<boolean>(ROLE_ASSIGN_PERMISSION_PATH(roleId, permissionId));
  return response.data;
}

export async function removePermissionFromRole(roleId: number, permissionId: number): Promise<boolean> {
  const response = await apiClient.delete<boolean>(ROLE_REMOVE_PERMISSION_PATH(roleId, permissionId));
  return response.data;
}

export async function getUserPermissionsByRole(userId: number): Promise<PermissionItem[]> {
  const response = await apiClient.get<PermissionItem[]>(`/api/roles/user/${userId}/permissions`);
  return response.data;
}
