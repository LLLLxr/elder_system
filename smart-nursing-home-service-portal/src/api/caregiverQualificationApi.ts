import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type { CaregiverQualificationApplication } from '../types/care';
import {
  QUALITY_CAREGIVER_QUALIFICATION_APPLICATIONS_PATH,
  QUALITY_CAREGIVER_QUALIFICATION_MY_APPLICATIONS_PATH,
} from './endpoints';

export async function submitCaregiverQualificationApplication(
  payload: CaregiverQualificationApplication,
): Promise<CaregiverQualificationApplication> {
  const response = await apiClient.post<ApiResponse<CaregiverQualificationApplication>>(QUALITY_CAREGIVER_QUALIFICATION_APPLICATIONS_PATH, payload);
  return unwrapApiData<CaregiverQualificationApplication>(response.data);
}

export async function listMyCaregiverQualificationApplications(): Promise<CaregiverQualificationApplication[]> {
  const response = await apiClient.get<ApiResponse<CaregiverQualificationApplication[]>>(QUALITY_CAREGIVER_QUALIFICATION_MY_APPLICATIONS_PATH);
  return unwrapApiData<CaregiverQualificationApplication[]>(response.data);
}
