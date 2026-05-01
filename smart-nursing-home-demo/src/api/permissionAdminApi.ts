import apiClient, { unwrapApiData } from './client';
import type {
  PermissionCreatePayload,
  PermissionItem,
  PermissionUpdatePayload,
} from '../types/admin';
import {
  PERMISSION_BY_ROLE_PATH,
  PERMISSION_DETAIL_PATH,
  PERMISSION_LIST_PATH,
  PERMISSION_STATUS_PATH,
  PERMISSION_TREE_PATH,
} from './endpoints';

export async function getPermissions(keyword?: string): Promise<PermissionItem[]> {
  const response = await apiClient.get<PermissionItem[] | { data?: PermissionItem[] }>(PERMISSION_LIST_PATH, {
    params: { keyword },
  });
  const payload = unwrapApiData<PermissionItem[]>(response.data);
  return Array.isArray(payload) ? payload : [];
}

export async function getPermissionTree(): Promise<PermissionItem[]> {
  const response = await apiClient.get<PermissionItem[] | { data?: PermissionItem[] }>(PERMISSION_TREE_PATH);
  const payload = unwrapApiData<PermissionItem[]>(response.data);
  return Array.isArray(payload) ? payload : [];
}

export async function getPermissionsByRole(roleId: number): Promise<PermissionItem[]> {
  const response = await apiClient.get<PermissionItem[] | { data?: PermissionItem[] }>(PERMISSION_BY_ROLE_PATH(roleId));
  const payload = unwrapApiData<PermissionItem[]>(response.data);
  return Array.isArray(payload) ? payload : [];
}

export async function createPermission(payload: PermissionCreatePayload): Promise<boolean> {
  const response = await apiClient.post<boolean>(PERMISSION_LIST_PATH, payload);
  return response.data;
}

export async function updatePermission(permissionId: number, payload: PermissionUpdatePayload): Promise<boolean> {
  const response = await apiClient.put<boolean>(PERMISSION_DETAIL_PATH(permissionId), payload);
  return response.data;
}

export async function updatePermissionStatus(permissionId: number, status: number): Promise<boolean> {
  const response = await apiClient.put<boolean>(PERMISSION_STATUS_PATH(permissionId), null, {
    params: { status },
  });
  return response.data;
}

export async function deletePermission(permissionId: number): Promise<boolean> {
  const response = await apiClient.delete<boolean>(PERMISSION_DETAIL_PATH(permissionId));
  return response.data;
}
