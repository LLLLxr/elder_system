import apiClient, { unwrapApiData } from './client';
import type { ElderBindingRequestItem, ElderBindingReviewPayload } from '../types/admin';
import {
  USER_ELDER_BINDING_REQUESTS_PATH,
  USER_ELDER_BINDING_REQUEST_DETAIL_PATH,
  USER_ELDER_BINDING_REQUEST_APPROVE_PATH,
  USER_ELDER_BINDING_REQUEST_REJECT_PATH,
} from './endpoints';

export async function listElderBindingRequests(status?: string): Promise<ElderBindingRequestItem[]> {
  const response = await apiClient.get(USER_ELDER_BINDING_REQUESTS_PATH, {
    params: { status },
  });
  return unwrapApiData<ElderBindingRequestItem[]>(response.data);
}

export async function getElderBindingRequestDetail(requestId: number): Promise<ElderBindingRequestItem> {
  const response = await apiClient.get(USER_ELDER_BINDING_REQUEST_DETAIL_PATH(requestId));
  return unwrapApiData<ElderBindingRequestItem>(response.data);
}

export async function approveElderBindingRequest(
  requestId: number,
  payload?: ElderBindingReviewPayload,
): Promise<ElderBindingRequestItem> {
  const response = await apiClient.post(USER_ELDER_BINDING_REQUEST_APPROVE_PATH(requestId), payload ?? {});
  return unwrapApiData<ElderBindingRequestItem>(response.data);
}

export async function rejectElderBindingRequest(
  requestId: number,
  payload?: ElderBindingReviewPayload,
): Promise<ElderBindingRequestItem> {
  const response = await apiClient.post(USER_ELDER_BINDING_REQUEST_REJECT_PATH(requestId), payload ?? {});
  return unwrapApiData<ElderBindingRequestItem>(response.data);
}
