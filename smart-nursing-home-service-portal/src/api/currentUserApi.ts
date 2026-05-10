import apiClient from './client';
import type {
  ElderBindingItem,
  ElderBindingRequestItem,
  FamilyElderBindingRequestCreatePayload,
  UserItem,
  UserUpdatePayload,
} from '../types/admin';
import {
  USER_ME_ELDER_BINDINGS_PATH,
  USER_ME_ELDER_BINDINGS_SELF_PATH,
  USER_ME_FAMILY_ELDER_BINDING_REQUESTS_PATH,
  USER_ME_PATH,
  USER_ME_ELDER_BINDING_REQUESTS_PATH,
} from './endpoints';

export async function getCurrentUserProfile(): Promise<UserItem> {
  const response = await apiClient.get<UserItem>(USER_ME_PATH);
  return response.data;
}

export async function listMyElderBindings(): Promise<ElderBindingItem[]> {
  const response = await apiClient.get<ElderBindingItem[]>(USER_ME_ELDER_BINDINGS_PATH);
  return response.data;
}

export async function updateCurrentUserProfile(payload: UserUpdatePayload): Promise<UserItem> {
  const response = await apiClient.put<UserItem>(USER_ME_PATH, payload);
  return response.data;
}

export async function createSelfElderBinding(): Promise<ElderBindingItem> {
  const response = await apiClient.post<ElderBindingItem>(USER_ME_ELDER_BINDINGS_SELF_PATH);
  return response.data;
}

export async function listMyElderBindingRequests(): Promise<ElderBindingRequestItem[]> {
  const response = await apiClient.get<ElderBindingRequestItem[]>(USER_ME_ELDER_BINDING_REQUESTS_PATH);
  return response.data;
}

export async function submitFamilyElderBindingRequest(
  payload: FamilyElderBindingRequestCreatePayload,
): Promise<ElderBindingRequestItem> {
  const response = await apiClient.post<ElderBindingRequestItem>(USER_ME_FAMILY_ELDER_BINDING_REQUESTS_PATH, payload);
  return response.data;
}
