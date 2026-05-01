import apiClient from './client';
import type {
  UserCreatePayload,
  UserItem,
  UserPageResponse,
  UserQueryParams,
  UserUpdatePayload,
} from '../types/admin';
import {
  USER_DETAIL_PATH,
  USER_LIST_PATH,
  USER_RESET_PASSWORD_PATH,
  USER_STATUS_PATH,
} from './endpoints';

export async function getUsers(params: UserQueryParams): Promise<UserPageResponse> {
  const response = await apiClient.get<UserPageResponse>(USER_LIST_PATH, { params });
  return response.data;
}

export async function createUser(payload: UserCreatePayload): Promise<UserItem> {
  const response = await apiClient.post<UserItem>(USER_LIST_PATH, payload);
  return response.data;
}

export async function updateUser(userId: number, payload: UserUpdatePayload): Promise<UserItem> {
  const response = await apiClient.put<UserItem>(USER_DETAIL_PATH(userId), payload);
  return response.data;
}

export async function deleteUser(userId: number): Promise<void> {
  await apiClient.delete(USER_DETAIL_PATH(userId));
}

export async function updateUserStatus(userId: number, status: number): Promise<void> {
  await apiClient.put(USER_STATUS_PATH(userId), null, { params: { status } });
}

export async function resetUserPassword(userId: number, newPassword: string): Promise<void> {
  await apiClient.put(USER_RESET_PASSWORD_PATH(userId), null, { params: { newPassword } });
}
