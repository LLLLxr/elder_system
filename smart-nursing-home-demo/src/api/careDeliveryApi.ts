import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type { NurseCareRecord, NurseCareRecordSaveRequest } from '../types/care';
import {
  CARE_DELIVERY_NURSE_CARE_RECORD_DETAIL_PATH,
  CARE_DELIVERY_NURSE_CARE_RECORDS_PATH,
} from './endpoints';

export async function listNurseCareRecords(params?: {
  elderId?: number;
  nurseId?: number;
  recordDate?: string;
}): Promise<NurseCareRecord[]> {
  const response = await apiClient.get<ApiResponse<NurseCareRecord[]>>(CARE_DELIVERY_NURSE_CARE_RECORDS_PATH, { params });
  return unwrapApiData<NurseCareRecord[]>(response.data);
}

export async function getNurseCareRecord(recordId: number): Promise<NurseCareRecord> {
  const response = await apiClient.get<ApiResponse<NurseCareRecord>>(CARE_DELIVERY_NURSE_CARE_RECORD_DETAIL_PATH(recordId));
  return unwrapApiData<NurseCareRecord>(response.data);
}

export async function createNurseCareRecord(payload: NurseCareRecordSaveRequest): Promise<NurseCareRecord> {
  const response = await apiClient.post<ApiResponse<NurseCareRecord>>(CARE_DELIVERY_NURSE_CARE_RECORDS_PATH, payload);
  return unwrapApiData<NurseCareRecord>(response.data);
}

export async function updateNurseCareRecord(
  recordId: number,
  payload: NurseCareRecordSaveRequest,
): Promise<NurseCareRecord> {
  const response = await apiClient.put<ApiResponse<NurseCareRecord>>(CARE_DELIVERY_NURSE_CARE_RECORD_DETAIL_PATH(recordId), payload);
  return unwrapApiData<NurseCareRecord>(response.data);
}
