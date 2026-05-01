import apiClient from './client';
import type { UserItem } from '../types/admin';
import { USER_ME_PATH } from './endpoints';

export async function getCurrentUserProfile(): Promise<UserItem> {
  const response = await apiClient.get<UserItem>(USER_ME_PATH);
  return response.data;
}
